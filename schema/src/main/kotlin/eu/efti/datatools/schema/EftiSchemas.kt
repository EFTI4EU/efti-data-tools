package eu.efti.datatools.schema

import org.apache.xmlbeans.SchemaTypeSystem
import org.apache.xmlbeans.XmlBeans
import org.apache.xmlbeans.XmlObject
import org.apache.xmlbeans.XmlOptions
import org.xml.sax.InputSource
import java.io.InputStream
import java.net.URL
import javax.xml.XMLConstants
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

private const val PATH_COMMON = "/consignment-common.xsd"

private const val PATH_IDENTIFIER = "/consignment-identifier.xsd"

object EftiSchemas {
    private val schemaFactory: SchemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)

    @JvmStatic
    val consignmentCommonSchema: XmlSchemaElement = readConsignmentCommonSchema()

    @JvmStatic
    val consignmentIdentifierSchema: XmlSchemaElement = readConsignmentIdentifierSchema()

    @JvmStatic
    val javaCommonSchema: Schema =
        schemaFactory.newSchema(getResourceUrl(PATH_COMMON))

    @JvmStatic
    val javaIdentifiersSchema: Schema =
        schemaFactory.newSchema(getResourceUrl(PATH_IDENTIFIER))

    private fun readConsignmentCommonSchema(): XmlSchemaElement = XmlSchemaParser.parse(
        readXmlBeansSchema(PATH_COMMON),
        XmlSchemaElement.XmlName("http://efti.eu/v1/consignment/common", "consignment"),
    )

    private fun readConsignmentIdentifierSchema(): XmlSchemaElement = XmlSchemaParser.parse(
        readXmlBeansSchema(PATH_IDENTIFIER),
        XmlSchemaElement.XmlName("http://efti.eu/v1/consignment/identifier", "consignment"),
    )

    private fun getXsdInputStream(relativePath: String): InputStream =
        checkNotNull(EftiSchemas::class.java.getResourceAsStream(relativePath)) {
            "Could not load resource: $relativePath"
        }

    private fun readXmlBeansSchema(mainSchemaRelativePath: String): SchemaTypeSystem {
        val mainXsd = getXsdInputStream(mainSchemaRelativePath)

        // When reading schema from input stream, XmlBeans will try to load referenced schemas (xsd:import) over
        // network by default. Let's define an entity resolver that resolves system ids of referenced schemas
        // into local resource input streams.
        val xmlOptions = XmlOptions().also {
            it.setEntityResolver { _, systemId ->
                InputSource(
                    if (systemId.startsWith("project://local/")) {
                        getXsdInputStream(systemId.removePrefix("project://local"))
                    } else {
                        getXsdInputStream("/$systemId")
                    },
                )
            }
        }

        return XmlBeans.compileXsd(
            arrayOf(XmlObject.Factory.parse(mainXsd, xmlOptions)),
            XmlBeans.getContextTypeLoader(),
            xmlOptions,
        )
    }

    private fun getResourceUrl(path: String): URL = checkNotNull(EftiSchemas::class.java.getResource(path)) {
        "Could not find resource $path"
    }
}
