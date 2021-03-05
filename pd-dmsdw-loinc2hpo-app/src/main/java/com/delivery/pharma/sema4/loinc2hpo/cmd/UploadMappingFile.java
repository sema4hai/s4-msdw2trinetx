package com.delivery.pharma.sema4.loinc2hpo.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

@Parameters(commandDescription = "Upload mapping files to DMSDW for analysis")
public class UploadMappingFile implements Command {

    private static Logger logger = LoggerFactory.getLogger(UploadMappingFile.class);

    public UploadMappingFile(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    private JdbcTemplate jdbcTemplate;

    @Parameter(names = "--labCode2Loinc", description = "specify the mapping file from local code to LOINC")
    private String labCode2LoincPath;
    @Parameter(names = "--loinc2Hpo", description = "specify the mapping file from LOINC to HPO")
    private String loinc2HpoPath;
    @Parameter(names = "--hpoTerms", description = "HPO term list as a flat file")
    private String hpoTermsPath;
    @Parameter(names = "--debug", description = "running in debug mode")
    private boolean debug = false;

    @Override
    public void run() {

        logger.info("loincMapping Path: " + labCode2LoincPath);
        logger.info("loinc2hpo path: " + loinc2HpoPath);
        logger.info("hpoTerms Path: " + hpoTermsPath);

        String schema = debug ? "pd_test_db" : "pd_prod_db";

        if (labCode2LoincPath != null){

        }

        if (loinc2HpoPath != null) {

        }

        if (hpoTermsPath != null) {

        }
    }

    public void uploadLabCode2Loinc(String filePath, JdbcTemplate jdbcTemplate, String schema){

    }

    public void uploadLoinc2Hpo(String filePath, JdbcTemplate jdbcTemplate, String schema){

    }

    public void uploadHpoTerms(String filePath, JdbcTemplate jdbcTemplate, String schema){
        jdbcTemplate.execute(String.format("DROP TABLE IF EXISTS %s.hpo", schema));
        String ddl = String.format("CREATE TABLE %s.hpo (termId VARCHAR(10), label VARCHAR)");
        jdbcTemplate.execute(ddl);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            // no header line
            String line;

            while ((line = reader.readLine()) != null){
                String[] fields = line.split(",");
                String sql = String.format("INSERT INTO %s.hpo values (?, ?)");
                //jdbcTemplate.query(sql, new Object[] {fields[0], fields[1]});
            }


        } catch (FileNotFoundException e){

        } catch (IOException e){

        }

    }
}
