package com.delivery.pharma.sema4.loinc2hpo;

import com.delivery.pharma.sema4.loinc2hpo.cmd.Command;
import com.delivery.pharma.sema4.loinc2hpo.cmd.HpoTermMap;
import com.delivery.pharma.sema4.loinc2hpo.cmd.InferHpoCmd;
import com.delivery.pharma.sema4.loinc2hpo.cmd.UploadMappingFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AppConfig {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Bean(name = "inferHpo")
    public Command inferHpo(){
        return new InferHpoCmd();
    }

    @Bean(name = "termMap")
    public Command termMap() {
        return new HpoTermMap();
    }

    @Bean(name = "uploadMappingFile")
    public Command uploadMappingFileCmd(JdbcTemplate jdbcTemplate) {
        return new UploadMappingFile(jdbcTemplate);
    }
}
