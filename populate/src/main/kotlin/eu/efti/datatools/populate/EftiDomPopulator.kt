package eu.efti.datatools.populate

import eu.efti.datatools.schema.XmlSchemaElement
import eu.efti.datatools.schema.XmlSchemaElement.XmlName
import eu.efti.datatools.schema.XmlSchemaElement.XmlType
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.max
import kotlin.math.min

typealias XmlValueGenerator = (valuePath: ValuePath, repeatIndex: Int, type: XmlType) -> String

enum class RepeatablePopulateMode {
    RANDOM,
    MINIMUM_ONE,
    EXACTLY_ONE,
}

@Suppress("detekt:MagicNumber")
class EftiDomPopulator(seed: Long, private val repeatableMode: RepeatablePopulateMode = RepeatablePopulateMode.RANDOM) {
    interface SchemaValueMatcher {
        fun match(name: XmlName, type: XmlType): Boolean
    }

    data class ValueNameMatcher(val localPart: String) : SchemaValueMatcher {
        override fun match(name: XmlName, type: XmlType) = name.localPart == localPart
    }

    data class ValueTypeMatcher(val typeLocalPart: String) : SchemaValueMatcher {
        override fun match(name: XmlName, type: XmlType) =
            type.name.localPart == typeLocalPart
    }

    object EnumTypeMatcher : SchemaValueMatcher {
        override fun match(name: XmlName, type: XmlType) =
            type.enumerationValues.isNotEmpty()
    }

    private val gen = EftiValueGeneratorFactory(seed)

    private val dateTimeFormatter205 = DateTimeFormatter.ofPattern("uuuuMMddHHmmxx")

    private val enumerationGenerator: XmlValueGenerator = { valuePath, _, type ->
        gen.forPath(valuePath).nextChoice(
            type.enumerationValues,
        )
    }

    private val generators: List<Pair<SchemaValueMatcher, XmlValueGenerator>> = listOf(
        ValueNameMatcher("schemeAgencyId") to noArgsGenerator { it.nextToken() },
        ValueNameMatcher("sequenceNumber") to repeatIndexGenerator { repeatIndex -> repeatIndex.toString() },
        ValueTypeMatcher("base64Binary") to noArgsGenerator {
            Base64.getEncoder().encodeToString(it.nextToken().toByteArray())
        },
        ValueTypeMatcher("boolean") to noArgsGenerator { it.nextBoolean().toString() },
        ValueTypeMatcher("DateTimeFormat") to noArgsGenerator {
            // Note: for simplicity, always use the same format
            "205"
        },
        ValueTypeMatcher("DateTime") to noArgsGenerator {
            // Note: for simplicity, always use the same format
            dateTimeFormatter205.format(OffsetDateTime.ofInstant(it.nextInstant(), ZoneOffset.UTC))
        },
        ValueTypeMatcher("decimal") to noArgsGenerator { String.format(Locale.UK, "%.2f", it.nextDouble(0.0, 10.0)) },
        ValueTypeMatcher("Identifier17") to noArgsGenerator { it.nextToken() },
        ValueTypeMatcher("integer") to noArgsGenerator { it.nextInt(1000, 9999).toString() },
        ValueTypeMatcher("string") to noArgsGenerator { it.nextToken(4) },
        EnumTypeMatcher to enumerationGenerator,
    )

    fun populate(doc: Document, schema: XmlSchemaElement) {
        val element = doc.appendChild(doc.createElementNS(schema.name.namespaceURI, schema.name.localPart))
        schema.children.forEach { child ->
            populateElement(doc, element, ValuePath(emptyList()).append(schema), child)
        }
    }

    private fun noArgsGenerator(block: (gen: EftiValueGeneratorFactory.EftiValueGenerator) -> String): XmlValueGenerator =
        { valuePath, _, _ -> block(gen.forPath(valuePath)) }

    private fun repeatIndexGenerator(block: (Int) -> String): XmlValueGenerator =
        { _, repeatIndex, _ -> block(repeatIndex) }

    private fun populateElement(doc: Document, parent: Node, parentPath: ValuePath, schema: XmlSchemaElement) {
        val currentPath = parentPath.append(schema)

        val repeatGenerator = gen.forPath(currentPath)
        val repeatRange = when (repeatableMode) {
            RepeatablePopulateMode.RANDOM ->
                schema.cardinality.min to repeatGenerator.nextLong(
                    0,
                    min(schema.cardinality.max ?: 3, 3)
                ) + max(schema.cardinality.min, 1)

            RepeatablePopulateMode.MINIMUM_ONE ->
                max(schema.cardinality.min, 1) to repeatGenerator.nextLong(
                    0,
                    min(schema.cardinality.max ?: 3, 3)
                ) + max(schema.cardinality.min, 2)

            RepeatablePopulateMode.EXACTLY_ONE -> 1 to 2
        }
        val count = repeatGenerator.nextLong(
            startInclusive = repeatRange.first.toLong(),
            endExclusive = repeatRange.second.toLong()
        )

        repeat(count.toInt()) { repeatIndex ->
            val element = parent.appendChild(doc.createElementNS(schema.name.namespaceURI, schema.name.localPart))

            schema.type.attributes.forEach { schemaAttribute ->
                val attribute =
                    doc.createAttributeNS(schemaAttribute.name.namespaceURI, schemaAttribute.name.localPart)

                if (schemaAttribute.type.isTextContentType) {
                    val generator = findMostSpecificGenerator(schemaAttribute.name, schemaAttribute.type)
                    attribute.value = generator(currentPath.append(schemaAttribute), 0, schemaAttribute.type)
                }
                element.attributes.setNamedItem(attribute)
            }

            schema.children.forEach { child ->
                populateElement(doc, element, currentPath.append(repeatIndex), child)
            }

            if (schema.type.isTextContentType) {
                val generator = findMostSpecificGenerator(schema.name, schema.type)
                element.textContent = generator(currentPath, repeatIndex, schema.type)
            }
        }
    }

    private fun findMostSpecificGenerator(name: XmlName, type: XmlType): XmlValueGenerator =
        requireNotNull(
            sequenceOf(type).plus(type.baseTypes)
                .firstNotNullOfOrNull { t ->
                    generators.firstOrNull { it.first.match(name, t) }?.second
                },
        ) {
            "No generator found for: $name $type"
        }
}
