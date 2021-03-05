package com.delivery.pharma.sema4.loinc2hpo.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.delivery.pharma.sema4.loinc2hpo.MyUtils;
import org.apache.commons.lang.StringUtils;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;

@Parameters(commandDescription = "Infer ancestor HPO terms")
public class InferHpoCmd implements Command {

    private final static Logger logger = LoggerFactory.getLogger(InferHpoCmd.class);

    @Parameter(names = {"--hpo"}, required = true)
    String hpoOboPath;

    @Parameter(names = {"-i", "--input"}, required = true, description = "lab result, must have loinc, interpretation")
    String inPath;

    @Parameter(names = {"--sep"}, required = false, description = "column separator")
    String SEP = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    @Parameter(names = {"-o", "--output"}, required = false, description = "filepath for results")
    String outPath;

    @Override
    public void run() {
        logger.info("start inferring HPO");
        logger.info("hpo path: " + hpoOboPath);
        logger.info("input path: " + inPath);
        Writer writer = MyUtils.getWriter(outPath);
        try {
            writer.write("study_id,encounter_key,age_in_days_key,hpo_term_id,is_inferred\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // i, load HPO
        Ontology hpo = OntologyLoader.loadOntology(new File(hpoOboPath));

        // ii, process lab result
        try (BufferedReader reader = new BufferedReader(new FileReader(inPath))){
            String line = reader.readLine();
            String[] colnames = line.split(SEP, -1);
            int NCOL = colnames.length;
            int LOINC_INDEX = -1;
            int INTERPRETATION_INDEX = -1;
            int HPOTERMID_INDEX = -1;
            int STUDYID_INDEX = -1;
            int ENCOUNTER_KEY_INDEX = -1;
            int AGE_IN_DAYS_KEY_INDEX = -1;
            for (int i = 0; i < NCOL; i++){
                if (colnames[i].equals("\"loinc\"")){
                    LOINC_INDEX = i;
                }
                if (colnames[i].equals("\"interpretation\"")){
                    INTERPRETATION_INDEX = i;
                }
                if (colnames[i].equals("\"hpoTermId\"")){
                    HPOTERMID_INDEX = i;
                }
                if (colnames[i].equals("\"study_id\"")){
                    STUDYID_INDEX = i;
                }
                if (colnames[i].equals("\"encounter_key\"")){
                    ENCOUNTER_KEY_INDEX = i;
                }
                if (colnames[i].equals("\"age_in_days_key\"")){
                    AGE_IN_DAYS_KEY_INDEX = i;
                }
            }
            if (LOINC_INDEX == -1 || INTERPRETATION_INDEX == -1 || HPOTERMID_INDEX == -1 ||
                    STUDYID_INDEX == -1 || ENCOUNTER_KEY_INDEX == -1 || AGE_IN_DAYS_KEY_INDEX == -1){
                throw new RuntimeException("LOINC, INTERPRETATION, HPOTERMID, STUDYID, ENCOUNTER_KEY or AGE_IN_DAYS_KEY " +
                        "column is not found");
            }
            logger.info(String.format("\nLOINC INDEX: %d\nINTERPRETATION INDEX: %d\nHPOTERMID INDEX: %d\n" +
                            "STUDYID INDEX: %d\nENCOUNTER_KEY: %d\nAGE_IN_DAYS_KEY: %d\n",
                    LOINC_INDEX, INTERPRETATION_INDEX, HPOTERMID_INDEX,
                    STUDYID_INDEX, ENCOUNTER_KEY_INDEX, AGE_IN_DAYS_KEY_INDEX));

            String[] fields;
            while ((line = reader.readLine()) != null){
                fields = line.split(SEP, -1);
                if (fields.length != NCOL){
                    System.out.println("line does not have specified number of elements:" + line);
                    throw new RuntimeException();
                }
                String loinc = fields[LOINC_INDEX];
                String interpretation = fields[INTERPRETATION_INDEX];
                String hpoTermIdString = fields[HPOTERMID_INDEX].replace("\"", "");

                if (hpoTermIdString.equals("NA")){
                    //abnormal finding cannot be mapped to HPO
                    Collection<String> out_elements = Arrays.asList(fields[STUDYID_INDEX], fields[ENCOUNTER_KEY_INDEX],
                            fields[AGE_IN_DAYS_KEY_INDEX], "\"NA\"", "\"NA\"");
                    writer.write(StringUtils.join(out_elements, ","));
                    writer.write("\n");
                } else {
                    TermId termId = TermId.of(hpoTermIdString);
                    Term term = hpo.getTermMap().get(termId);
                    if (term == null){
                        // This basically means the current hp.obo is out of dated
                        System.out.println("Cannot find term for : " + termId);
                        throw new RuntimeException();
                    } else {
                        Collection<String> out_elements = Arrays.asList(fields[STUDYID_INDEX], fields[ENCOUNTER_KEY_INDEX],
                                fields[AGE_IN_DAYS_KEY_INDEX], fields[HPOTERMID_INDEX], "\"N\"");
                        writer.write(StringUtils.join(out_elements, ','));
                        writer.write("\n");
                        // set false to exclude root term
                        Collection<TermId> ancestors = hpo.getAncestorTermIds(termId, false);
                        for (TermId ancestor : ancestors){
                            // remove term itself from its ancestors
                            if (ancestor.equals(termId)){
                                continue;
                            }
                            out_elements = Arrays.asList(fields[STUDYID_INDEX], fields[ENCOUNTER_KEY_INDEX],
                                    fields[AGE_IN_DAYS_KEY_INDEX], String.format("\"%s\"", ancestor.toString()), "\"Y\"");
                            writer.write(StringUtils.join(out_elements, ','));
                            writer.write("\n");
                        }
                    }

                }
            }

        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("end inferring HPO");
    }
}

