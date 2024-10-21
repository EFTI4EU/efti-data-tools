package eu.efti.datatools.app

import eu.efti.datatools.populate.EftiDomPopulator
import eu.efti.datatools.populate.RepeatablePopulateMode
import eu.efti.datatools.schema.EftiSchemas
import org.w3c.dom.Document
import org.w3c.dom.bootstrap.DOMImplementationRegistry
import org.w3c.dom.ls.DOMImplementationLS
import java.io.ByteArrayOutputStream
import javax.xml.parsers.DocumentBuilderFactory

fun main() {
    println("Hello World!")
    val pop = EftiDomPopulator(42, RepeatablePopulateMode.MINIMUM_ONE)
    val doc = newDocument()
    pop.populate(doc, EftiSchemas.readConsignmentCommonSchema())
    println(serializeToString(doc))
}

private fun newDocument(): Document {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    return builder.newDocument()
}

private fun serializeToString(doc: Document, prettyPrint: Boolean = false): String {
    val registry = DOMImplementationRegistry.newInstance()
    val domImplLS = registry.getDOMImplementation("LS") as DOMImplementationLS

    val lsSerializer = domImplLS.createLSSerializer()
    val domConfig = lsSerializer.domConfig
    domConfig.setParameter("format-pretty-print", prettyPrint)

    val byteArrayOutputStream = ByteArrayOutputStream()
    val lsOutput = domImplLS.createLSOutput()
    lsOutput.encoding = "UTF-8"
    lsOutput.byteStream = byteArrayOutputStream

    lsSerializer.write(doc, lsOutput)
    return byteArrayOutputStream.toString(Charsets.UTF_8)
}
