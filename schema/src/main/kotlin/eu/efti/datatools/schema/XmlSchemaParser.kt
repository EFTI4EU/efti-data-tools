package eu.efti.datatools.schema

import eu.efti.datatools.schema.XmlSchemaElement.*
import org.apache.xmlbeans.SchemaLocalElement
import org.apache.xmlbeans.SchemaParticle
import org.apache.xmlbeans.SchemaType
import org.apache.xmlbeans.SchemaTypeSystem

object XmlSchemaParser {
    fun parse(xmlBeansSchema: SchemaTypeSystem, documentType: XmlName): XmlSchemaElement {
        val documentSchema = checkNotNull(
            xmlBeansSchema.documentTypes().find {
                it.contentModel.name.namespaceURI == documentType.namespaceURI && it.contentModel.name.localPart == documentType.localPart
            },
        ) { "Not found: $documentType" }

        require(documentSchema.contentType == SchemaType.ELEMENT_CONTENT) { "Unsupported document type ${documentSchema.contentType}" }

        return parseElement(documentSchema.contentModel)
    }

    private fun parseElement(particle: SchemaParticle): XmlSchemaElement {
        require(particle.particleType == SchemaParticle.ELEMENT) { "Unsupported particle type ${particle.particleType}" }

        val base = toXmlElement((particle as SchemaLocalElement))

        val schemaType = (particle as SchemaParticle).type
        val schemaContentModel = schemaType?.contentModel
        val children = if (schemaContentModel != null) {
            when (schemaContentModel.particleType) {
                SchemaParticle.ALL, SchemaParticle.CHOICE, SchemaParticle.SEQUENCE -> {
                    val children = schemaContentModel.particleChildren
                    children.map { c -> parseElement(c) }
                }

                SchemaParticle.ELEMENT -> listOf(parseElement(schemaContentModel))

                else -> null
            }
        } else {
            null
        }

        return if (children != null) base.copy(children = children) else base
    }

    private fun toXmlElement(schemaElement: SchemaLocalElement): XmlSchemaElement = XmlSchemaElement(
        name = XmlName(schemaElement.name.namespaceURI, schemaElement.name.localPart),
        type = toXmlType(schemaElement.type),
        cardinality = XmlCardinality(
            schemaElement.minOccurs?.longValueExact() ?: 1,
            schemaElement.maxOccurs?.longValueExact(),
        ),
        children = emptyList(),
    )

    private fun toXmlType(type: SchemaType): XmlType {
        val formattedAttrs = type.attributeProperties?.map { attr ->
            XmlAttribute(
                name = XmlName(attr.name.namespaceURI, attr.name.localPart),
                type = XmlType(
                    name = XmlName(attr.type.name.namespaceURI, attr.type.name.localPart),
                    enumerationValues = attr.type.enumerationValues
                        ?.map { e -> e.stringValue }
                        ?: emptyList(),
                    attributes = emptyList(),
                    baseTypes = collectBaseTypes(attr.type, emptyList()),
                    isTextContentType = isTextContentType(attr.type),
                ),
            )
        } ?: emptyList()

        return XmlType(
            name = XmlName(
                type.name.namespaceURI,
                type.name.localPart,
            ),
            enumerationValues = type.enumerationValues
                ?.map { e -> e.stringValue }
                ?: emptyList(),
            attributes = formattedAttrs,
            baseTypes = collectBaseTypes(type, emptyList()),
            isTextContentType = isTextContentType(type),
        )
    }

    private fun isTextContentType(schemaType: SchemaType) =
        schemaType.contentType == SchemaType.SIMPLE_CONTENT || schemaType.isSimpleType

    private tailrec fun collectBaseTypes(type: SchemaType, accumulator: List<XmlType>): List<XmlType> {
        val base = type.baseType
        // Simplify base types list by leaving out the "anyType".
        return if (base != null && !(base.name.namespaceURI == "http://www.w3.org/2001/XMLSchema" && base.name.localPart == "anyType")) {
            collectBaseTypes(base, accumulator + toXmlType(base))
        } else {
            accumulator
        }
    }
}
