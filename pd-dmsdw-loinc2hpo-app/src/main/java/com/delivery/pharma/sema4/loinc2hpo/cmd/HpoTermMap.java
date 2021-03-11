package com.delivery.pharma.sema4.loinc2hpo.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.delivery.pharma.sema4.loinc2hpo.MyUtils;
import org.apache.commons.lang.StringUtils;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
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
        Map<TermId, Term> termMap = hpo.getTermMap();
        List<String> termMapEntry = termMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), e-> e.getValue().getName()))
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + "," + entry.getValue())
                .collect(Collectors.toList());
        try {
            writer.write(StringUtils.join(termMapEntry, '\n'));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
