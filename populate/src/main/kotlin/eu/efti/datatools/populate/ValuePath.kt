package eu.efti.datatools.populate

import eu.efti.datatools.schema.XmlSchemaElement

data class ValuePath(val path: List<String>) {
    fun append(repeatIndex: Int) = ValuePath(path + repeatIndex.toString())

    fun append(element: XmlSchemaElement) = ValuePath(path + element.name.localPart)

    fun append(attribute: XmlSchemaElement.XmlAttribute) = ValuePath(path + attribute.name.localPart)
}
