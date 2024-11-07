package eu.efti.datatools.populate

import eu.efti.datatools.schema.EftiSchemas
import eu.efti.datatools.schema.XmlSchemaElement
import eu.efti.datatools.schema.XmlUtil
import eu.efti.datatools.schema.XmlUtil.serializeToString
import eu.efti.datatools.testsupport.DocumentExpectationTestUtil.assertMatchesDocument
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.w3c.dom.Document
import java.io.*
import javax.xml.validation.Schema
import kotlin.streams.asStream

@Suppress("SameParameterValue")
class EftiDomPopulatorTest {
    @ParameterizedTest
    @MethodSource("populateTestCases")
    fun `should create valid documents with different generator seeds`(testCase: PopulateTestCase) {
        val populator = EftiDomPopulator(testCase.seed, testCase.repeatablePopulateMode)
        val doc = populator.populate(testCase.eftiSchema)

        val error = XmlUtil.validate(doc, testCase.javaSchema)

        // Optimization: catch assertion error so that we can generate full error message lazily
        try {
            assertThat(error, nullValue())
        } catch (e: AssertionError) {
            fail(e.message + "\nDocument was:\n ${formatXml(doc)}", e)
        }
    }

    @Test
    @Tag("expectation-update")
    fun `should populate common document that matches the expected document`() {
        val eftiSchema = EftiSchemas.readConsignmentCommonSchema()

        val populator = EftiDomPopulator(42, RepeatablePopulateMode.MINIMUM_ONE)

        assertMatchesDocument(
            caller = EftiDomPopulatorTest::class,
            expectationFilename = "common-expected.xml",
            actual = populator.populate(eftiSchema)
        ) { actual, _ ->
            listOf(
                Executable { assertThat(XmlUtil.validate(actual, EftiSchemas.javaCommonSchema), nullValue()) },
            )
        }
    }

    @Test
    @Tag("expectation-update")
    fun `should apply overrides in order`() {
        val seed = 23L
        val repeatableMode = RepeatablePopulateMode.MINIMUM_ONE

        val overrides = listOf(
            "consignment/deliveryEvent/actualOccurrenceDateTime" to "000000000000+0000",
            "/consignment/mainCarriageTransportMovement/modeCode" to "8",
        )
            .map { (expression, value) ->
                expression to EftiDomPopulator.TextContentOverride.tryToParse(expression, value)
            }
            .onEach { (expression, parsed) -> if (parsed == null) throw IllegalArgumentException("""Could not parse "$expression"""") }
            .mapNotNull(Pair<String, EftiDomPopulator.TextContentOverride?>::second)

        val populator = EftiDomPopulator(seed, repeatableMode)

        assertMatchesDocument(
            caller = EftiDomPopulatorTest::class,
            expectationFilename = "override-expected.xml",
            actual = populator.populate(
                EftiSchemas.readConsignmentIdentifiersSchema(),
                overrides,
                namespaceAware = false
            )
        ) { actual, _ ->
            listOf(
                Executable { assertThat(XmlUtil.validate(actual, EftiSchemas.javaIdentifiersSchema), nullValue()) },
            )
        }
    }

    companion object {
        data class PopulateTestCase(
            val schemaVariant: String,
            val seed: Long,
            val repeatablePopulateMode: RepeatablePopulateMode,
            val eftiSchema: XmlSchemaElement,
            val javaSchema: Schema,
        ) {
            override fun toString(): String =
                """schemaVariant=$schemaVariant, seed=$seed, repeatablePopulateMode=$repeatablePopulateMode"""
        }

        @JvmStatic
        fun populateTestCases(): java.util.stream.Stream<PopulateTestCase> =
            populateTestCasesForVariant("identifier").plus(populateTestCasesForVariant("common")).asStream()

        private fun populateTestCasesForVariant(schemaVariant: String): Sequence<PopulateTestCase> {
            val (javaSchema, eftiSchema) = when (schemaVariant) {
                "common" -> EftiSchemas.javaCommonSchema to EftiSchemas.readConsignmentCommonSchema()
                "identifier" -> EftiSchemas.javaIdentifiersSchema to EftiSchemas.readConsignmentIdentifiersSchema()
                else -> throw IllegalArgumentException(schemaVariant)
            }

            return (1..100).asSequence().map { seed ->
                PopulateTestCase(
                    schemaVariant = schemaVariant,
                    seed = seed.toLong(),
                    repeatablePopulateMode = when (seed % 3) {
                        0 -> RepeatablePopulateMode.RANDOM
                        1 -> RepeatablePopulateMode.MINIMUM_ONE
                        2 -> RepeatablePopulateMode.EXACTLY_ONE
                        else -> RepeatablePopulateMode.RANDOM
                    },
                    eftiSchema = eftiSchema,
                    javaSchema = javaSchema,
                )
            }
        }

        private fun formatXml(doc: Document): String = serializeToString(doc, prettyPrint = true)
    }
}
