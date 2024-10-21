package eu.efti.datatools.schema

import eu.efti.datatools.schema.EftiSchemas.readConsignmentCommonSchema
import eu.efti.datatools.schema.EftiSchemas.readConsignmentIdentifiersSchema
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class EftiSchemaTest {
    @Test
    fun `should parse identifier schema to XmlSchemaElement`() {
        val element = readConsignmentIdentifiersSchema()

        // Do some minimal assertions.
        assertAll(
            { assertThat(element.name.localPart, equalTo("consignment")) },
            { assertThat(element.children, hasSize(4)) },
        )
    }

    @Test
    fun `should parse common schema to XmlSchemaElement`() {
        val element = readConsignmentCommonSchema()

        // Do some minimal assertions.
        assertAll(
            { assertThat(element.name.localPart, equalTo("consignment")) },
            { assertThat(element.children, hasSize(42)) },
        )
    }
}
