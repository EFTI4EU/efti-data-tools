package eu.efti.datatools.app

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import eu.efti.datatools.populate.EftiDomPopulator
import eu.efti.datatools.populate.RepeatablePopulateMode
import eu.efti.datatools.schema.EftiSchemas
import org.w3c.dom.Document
import org.w3c.dom.bootstrap.DOMImplementationRegistry
import org.w3c.dom.ls.DOMImplementationLS
import java.io.ByteArrayOutputStream
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.system.exitProcess

class Args {
    @Parameter(names = ["--help", "-h"], help = true, description = "Print this help.")
    var help = false

    @Parameter(
        names = ["--seed", "-s"],
        description = "Seed to use, by default a random seed is used. Identical seeds should produce identical documents for a given version of the application."
    )
    var seed: Long? = null

    @Parameter(names = ["--output", "-o"], required = false, description = "Output file (will not be overwritten).")
    var outputCommon: String = "consignment-common.xml"

    @Parameter(names = ["--pretty", "-p"], required = false, description = "Pretty print.")
    var pretty: Boolean = true
}

fun main(argv: Array<String>) {
    val args = Args()
    val parser = JCommander.newBuilder()
        .addObject(args)
        .build()
    parser.parse(*argv)

    if (args.help) {
        parser.usage()
    } else {
        val seed = args.seed ?: randomShortSeed()

        println(
            """|Generating with:
               |  * seed: $seed
               |  * output: ${args.outputCommon}
               |  * pretty: ${args.pretty}
           """.trimMargin()
        )

        val file = File(args.outputCommon)
        if (file.exists()) {
            println("Output file ${args.outputCommon} already exists")
            exitProcess(1)
        } else {
            val doc = newDocument().also {
                EftiDomPopulator(seed, RepeatablePopulateMode.MINIMUM_ONE)
                    .populate(it, EftiSchemas.readConsignmentCommonSchema())
            }

            file.printWriter().use { out ->
                out.print(serializeToString(doc, prettyPrint = args.pretty))
            }
        }
    }
}

private fun randomShortSeed() =
    System.currentTimeMillis().let { number -> number - (number / 100000 * 100000) }

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
