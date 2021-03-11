package com.delivery.pharma.sema4.loinc2hpo.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.delivery.pharma.sema4.loinc2hpo.MyUtils;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

@Parameters(commandDescription = "Generate HPO term pairs with an is_a relationship")
public class HpoIsAPairsCmd implements Command {

    private final static Logger logger = LoggerFactory.getLogger(InferHpoCmd.class);

    @Parameter(names = {"--hpo"}, required = true)
    String hpoOboPath;

    @Parameter(names = {"-o", "--output"}, required = false, description = "filepath for results")
    String outPath;

    @Override
    public void run() {

        Ontology hpo = OntologyLoader.loadOntology(new File(hpoOboPath));
        Writer writer = MyUtils.getWriter(outPath);

        hpoIsAPairs(hpo, writer);

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Output HPO is_a pairs: current, ancestor, distance
     * where distance measures how many jumps to reach ancestor from current term.
     * @param hpo
     */
    public void hpoIsAPairs(Ontology hpo, Writer writer) {
        String header = "current,ancestor,distance";
        try {
            writer.write(header + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collection<TermId> terms = hpo.getAllTermIds();

        for (TermId term : terms){
            Collection<TermId> ancestors = hpo.getAncestorTermIds(term, false);
            for (TermId ancestor : ancestors){
                if (ancestor.equals(term)){
                    continue;
                } else {
                    // TODO: calculate distance from current term to ancestor
                    int distance = -1;
                    try {
                        writer.write(String.format("%s,%s,%d\n", term.getValue(), ancestor.getValue(), distance));
                        logger.debug(String.format("%s, %s\n", hpo.getTermLabel(term).get(), hpo.getTermLabel(ancestor).get()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        try {
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
