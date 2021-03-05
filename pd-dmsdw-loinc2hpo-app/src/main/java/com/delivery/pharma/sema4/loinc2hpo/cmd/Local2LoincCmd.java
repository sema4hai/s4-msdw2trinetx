package com.delivery.pharma.sema4.loinc2hpo.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Infer ancestor HPO terms")
public class Local2LoincCmd implements Command {

    @Parameter(names = {"-i", "--input"}, required = true, description = "lab test results extracted from DMSDW")
    String inPath;

    @Parameter(names = {"--sep"}, required = false, description = "column separator")
    String SEP = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    @Parameter(names = {"-o", "--output"}, required = false, description = "filepath for results")
    String outPath;

    @Override
    public void run() {

        //1. process procedure_key dictionary: procedure_key -> local (context_name, local_key)

        //2. iterate input file to add the LOINC code

    }
}
