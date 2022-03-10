package com.delivery.pharma.sema4.loinc2hpo.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Parameters(commandDescription = "Transform lab tests into HPO terms")
public class Loinc2HpoCmd implements Command {

    private final static Logger logger = LoggerFactory.getLogger(InferHpoCmd.class);

    @Parameter(names = {"--schema"}, required = true)
    String schema;

    @Autowired
    DataSource dataSource;

    @Override
    public void run() {

        if (schema.equals("MSDW")){
            for_MSDW();
        } else{
            System.err.println("Wrong schema is specified");
        }

    }


    public void for_MSDW(){
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/loinc2hpo_SCC_MSDW.sql"));
        populator.addScript(new ClassPathResource("sql/loinc2hpo_Epic_MSDW.sql"));
        populator.addScript(new ClassPathResource("sql/loinc2hpo_combine_Epic_SCC.sql"));

        populator.execute(dataSource);
    }
}
