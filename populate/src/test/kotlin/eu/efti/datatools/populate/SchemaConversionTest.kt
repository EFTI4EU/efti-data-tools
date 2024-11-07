package eu.efti.datatools.populate

import eu.efti.datatools.populate.SchemaConversion.commonToIdentifiers
import eu.efti.datatools.schema.EftiSchemas
import eu.efti.datatools.schema.XmlUtil
import eu.efti.datatools.testsupport.DocumentExpectationTestUtil.assertMatchesDocument
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class SchemaConversionTest {
    @Test
    @Tag("expectation-update")
    fun `should convert common document to identifiers document that matches expected document`() {
        assertMatchesDocument(
            caller = SchemaConversionTest::class,
            expectationFilename = "conversion-expected.xml",
            actual = commonToIdentifiers(
                EftiDomPopulator(1234, RepeatablePopulateMode.MINIMUM_ONE).populate(
                    EftiSchemas.readConsignmentCommonSchema()
                )
            ),
            additionalAssertions = { actual, _ ->
                listOf(
                    Executable { assertThat(XmlUtil.validate(actual, EftiSchemas.javaIdentifiersSchema), nullValue()) },
                )
            }
        )
    }
}
