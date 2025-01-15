package eu.efti.datatools.javaexample;

import eu.efti.datatools.populate.EftiDomPopulator;
import eu.efti.datatools.populate.RepeatablePopulateMode;
import eu.efti.datatools.schema.EftiSchemas;
import eu.efti.datatools.schema.XmlUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class JavaExampleTest {
    @Test
    public void shouldFilterSubsetsOnPopulatedDocument() {
        var populator = new EftiDomPopulator(1234, RepeatablePopulateMode.MINIMUM_ONE);
        var originalDoc = populator.populate(EftiSchemas.getConsignmentIdentifierSchema(), List.of(), true);

        var filteredDoc = JavaExample.filterIdentifierSubsets(originalDoc, Set.of("FI01", "FI02"));

        var originalXml = XmlUtil.serializeToString(originalDoc, true);
        var filteredXml = XmlUtil.serializeToString(filteredDoc, true);

        System.out.println("### Original");
        System.out.println(originalXml);
        System.out.println("### Filtered");
        System.out.println(filteredXml);

        assertNotEquals(originalXml, filteredXml);
    }
}
