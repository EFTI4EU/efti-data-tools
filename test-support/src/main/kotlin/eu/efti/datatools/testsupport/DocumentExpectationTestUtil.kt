package eu.efti.datatools.testsupport

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.function.Executable
import org.w3c.dom.Document
import org.w3c.dom.bootstrap.DOMImplementationRegistry
import org.w3c.dom.ls.DOMImplementationLS
import java.io.*
import kotlin.reflect.KClass

object DocumentExpectationTestUtil {
    private val updateTestExpectations: Boolean = System.getenv("eu.efti.updateTestExpectations") == "true"

    fun assertMatchesDocument(
        caller: KClass<*>,
        expectationFilename: String,
        actual: Document,
        additionalAssertions: (Document, String) -> Iterable<Executable>
    ) {
        if (updateTestExpectations) {
            val updated = formatXml(actual)
            FileWriter(testResourceFile(caller, expectationFilename)).use {
                it.write(updated)
                it.flush()
            }
            fail<Nothing>("Test expectations updated, run test again to verify")
        } else {
            val expected =
                InputStreamReader(classpathInputStream(caller, expectationFilename)).use { it.readText() }

            assertAll(
                listOf(
                    Executable {
                        // Use junit assertEquals because it formats the expected value better than hamcrest.
                        Assertions.assertEquals(
                            expected, formatXml(actual),
                            "Populated document did not match the expected document, please update test expectations with: ./gradlew updateTestExpectations"
                        )
                    }
                )
                    .plus(additionalAssertions(actual, expected))
            )
        }
    }

    fun withDocumentExpectation3(
        caller: KClass<*>,
        expectationFilename: String,
        actual: Document,
        block: (Document, String) -> Iterable<Executable>
    ) {
        if (updateTestExpectations) {
            val updated = formatXml(actual)
            FileWriter(testResourceFile(caller, expectationFilename)).use {
                it.write(updated)
                it.flush()
            }
            fail<Nothing>("Test expectations updated, run test again to verify")
        } else {
            val expected =
                InputStreamReader(classpathInputStream(caller, expectationFilename)).use { it.readText() }

            assertAll(
                listOf(
                    Executable {
                        // Use junit assertEquals because it formats the expected value better than hamcrest.
                        Assertions.assertEquals(
                            expected, formatXml(actual),
                            "Populated document did not match the expected document, please update test expectations with: ./gradlew updateTestExpectations"
                        )
                    }
                )
                    .plus(block(actual, expected))
            )

//            DocumentExpectationContextImpl.block(actual, expected)
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

    private fun classpathInputStream(caller: KClass<*>, filename: String): InputStream =
        checkNotNull(caller.java.getResourceAsStream(filename)) {
            "Could not open $filename"
        }

    private fun testResourceFile(caller: KClass<*>, filename: String): File {
        val packagePath = caller.java.packageName.replace(".", "/")
        return File("""src/test/resources/$packagePath/$filename""")
    }
}