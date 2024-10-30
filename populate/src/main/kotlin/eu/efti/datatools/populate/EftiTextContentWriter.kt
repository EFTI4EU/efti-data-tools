package eu.efti.datatools.populate

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

object EftiTextContentWriter {
    private val xpathFactory = XPathFactory.newInstance()

    fun setTextContent(doc: Document, xpath: String, value: String) {
        val expression = compileXpath(xpath)
        val nodes = expression.evaluate(doc, XPathConstants.NODESET) as NodeList
        nodes.asIterable().forEach { node -> node.textContent = value }
    }

    private fun compileXpath(expression: String): XPathExpression {
        val xpath = xpathFactory.newXPath()
        return xpath.compile(expression)
    }

    private fun NodeList.asIterable(): Iterable<Node> =
        (0 until this.length).asSequence().map { this.item(it) }.asIterable()

}