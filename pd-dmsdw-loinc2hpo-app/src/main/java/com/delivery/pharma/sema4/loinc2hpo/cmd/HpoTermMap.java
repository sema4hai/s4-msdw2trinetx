package com.delivery.pharma.sema4.loinc2hpo.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.delivery.pharma.sema4.loinc2hpo.MyUtils;
import org.apache.commons.lang.StringUtils;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.algo.ShortestPathTable;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Parameters(commandDescription = "Generate HPO term map file")
public class HpoTermMap implements Command {


    private final static Logger logger = LoggerFactory.getLogger(InferHpoCmd.class);

    @Parameter(names = {"--hpo"}, required = true)
    String hpoOboPath;

    @Parameter(names = {"-o", "--output"}, required = false, description = "filepath for results")
    String outPath;


    @Override
    public void run() {

        Ontology hpo = OntologyLoader.loadOntology(new File(hpoOboPath));
        Writer writer = MyUtils.getWriter(outPath);

        hpoTermId2Label(hpo, writer);

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void hpoTermId2Label(Ontology hpo, Writer writer){
        TermId PHENOTYPICALLY_ABNORMALITY = TermId.of("HP:0000118");
        // @TODO: this method is not working when a term have multiple parents; figure out why
        // seems these terms are actually alternative ids
        // now very few terms remain having -1 for their distance to root, but they are not phenotype terms
        ShortestPathTable shortestPathTable = new ShortestPathTable(hpo);
        Map<TermId, Term> termMap = hpo.getTermMap();
        List<String> termMapEntry = termMap.entrySet().stream()
                .filter(entry -> !entry.getValue().isObsolete())
                .map(entry -> entry.getKey().toString() + "," + shortestPathTable.getDistance(entry.getValue().getId(), PHENOTYPICALLY_ABNORMALITY) + "," + entry.getValue().getName() )
                .collect(Collectors.toList());
        try {
            writer.write(StringUtils.join(termMapEntry, '\n'));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
