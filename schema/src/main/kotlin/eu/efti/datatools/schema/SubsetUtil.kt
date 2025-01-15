package eu.efti.datatools.schema

import eu.efti.datatools.schema.EftiSchemas.consignmentCommonSchema
import eu.efti.datatools.schema.EftiSchemas.consignmentIdentifierSchema
import eu.efti.datatools.schema.EftiSchemas.javaCommonSchema
import eu.efti.datatools.schema.EftiSchemas.javaIdentifiersSchema
import eu.efti.datatools.schema.XmlUtil.clone
import eu.efti.datatools.schema.XmlUtil.dropNodesRecursively
import eu.efti.datatools.schema.XmlUtil.validate
import org.w3c.dom.Document
import org.w3c.dom.Node
import javax.xml.validation.Schema

object SubsetUtil {
    @JvmStatic
    fun filterCommonSubsets(doc: Document, subsets: Set<XmlSchemaElement.SubsetId>): Document =
        filterSubsets(doc, subsets, javaCommonSchema, consignmentCommonSchema)

    @JvmStatic
    fun filterIdentifierSubsets(doc: Document, subsets: Set<XmlSchemaElement.SubsetId>): Document =
        filterSubsets(doc, subsets, javaIdentifiersSchema, consignmentIdentifierSchema)

    @JvmStatic
    fun dropNodesNotInSubsets(subsets: Set<XmlSchemaElement.SubsetId>, schema: XmlSchemaElement, node: Node) {
        require(subsets.isNotEmpty()) {
            "subsets must be non-empty"
        }

        dropNodesRecursively(
            schema = schema,
            node = node,
            namespaceAware = true
        ) { candidateNode: Node, maybeSchemaElement: XmlSchemaElement? ->
            val schemaElement = checkNotNull(maybeSchemaElement) {
                "Schema element for ${candidateNode.localName} must not be null"
            }

            val isInRequestedSubsets = schemaElement.subsets.any { subset -> subset in subsets }
            !isInRequestedSubsets
        }
    }

    private fun filterSubsets(
        doc: Document, subsets: Set<XmlSchemaElement.SubsetId>, javaSchema: Schema, schema: XmlSchemaElement
    ): Document {
        return validate(doc, javaSchema)
            ?.let { error -> throw IllegalArgumentException("Input document is not valid: $error") }
            ?: clone(doc)
                .also { cloned ->
                    dropNodesNotInSubsets(subsets, schema, cloned.firstChild)
                    validate(cloned, javaSchema)
                }
    }
}
