package eu.efti.datatools.populate

import eu.efti.datatools.schema.EftiSchemas
import eu.efti.datatools.schema.XmlSchemaElement
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.w3c.dom.Document
import org.w3c.dom.bootstrap.DOMImplementationRegistry
import org.w3c.dom.ls.DOMImplementationLS
import org.xml.sax.SAXException
import java.io.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Source
import javax.xml.transform.dom.DOMSource
import javax.xml.validation.Schema
import kotlin.streams.asStream

@Suppress("SameParameterValue")
class EftiDomPopulatorTest {
    private val updateTestExpectations: Boolean = System.getenv("eu.efti.updateTestExpectations") == "true"

    @ParameterizedTest
    @MethodSource("populateTestCases")
    fun `should create valid documents with different generator seeds`(testCase: PopulateTestCase) {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()

        val doc: Document = builder.newDocument()

        val populator = EftiDomPopulator(testCase.seed, testCase.repeatablePopulateMode)
        populator.populate(doc, testCase.eftiSchema)

        val error = validate(doc, testCase.javaSchema)

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

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()

        val doc: Document = builder.newDocument()

        val populator = EftiDomPopulator(42, RepeatablePopulateMode.EXACTLY_ONE)
        populator.populate(doc, eftiSchema)

        val expectationFilename = "common-expected.xml"
        val expected =
            InputStreamReader(classpathInputStream(expectationFilename)).use { it.readText() }

        if (updateTestExpectations) {
            val updated = formatXml(doc)
            FileWriter(testResourceFile(expectationFilename)).use {
                it.write(updated)
                it.flush()
            }
            fail("Test expectations updated, run test again to verify")
        } else {
            // Use junit assertEquals because it formats the expected value better than hamcrest
            assertEquals(
                expected, formatXml(doc),
                "Populated document did not match the expected document, please update test expectations with: ./gradlew updateTestExpectations"
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

        private fun formatXml(doc: Document): String {
            val registry = DOMImplementationRegistry.newInstance()
            val domImplLS = registry.getDOMImplementation("LS") as DOMImplementationLS

            val lsSerializer = domImplLS.createLSSerializer()
            val domConfig = lsSerializer.domConfig
            domConfig.setParameter("format-pretty-print", true)

            val byteArrayOutputStream = ByteArrayOutputStream()
            val lsOutput = domImplLS.createLSOutput()
            lsOutput.encoding = "UTF-8"
            lsOutput.byteStream = byteArrayOutputStream

            lsSerializer.write(doc, lsOutput)
            return byteArrayOutputStream.toString(Charsets.UTF_8)
        }

        private fun validate(doc: Document, javaSchema: Schema): String? {
            val xmlSource: Source = DOMSource(doc)
            val error = try {
                javaSchema.newValidator().validate(xmlSource)
                null
            } catch (e: SAXException) {
                e.message
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            return error
        }

        private fun classpathInputStream(filename: String): InputStream =
            checkNotNull(EftiDomPopulatorTest::class.java.getResourceAsStream(filename)) {
                "Could not open $filename"
            }

        private fun testResourceFile(filename: String): File {
            val packagePath = EftiDomPopulatorTest::class.java.packageName.replace(".", "/")
            return File("""src/test/resources/$packagePath/$filename""")
        }
    }
}
