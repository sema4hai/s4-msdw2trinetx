package com.delivery.pharma.sema4.loinc2hpo.s4_pd_dmsdw;

import com.delivery.pharma.sema4.loinc2hpo.s4_pd_dmsdw.cmd.DMSDW2TriNetXLiteCmd;
import com.delivery.pharma.sema4.loinc2hpo.s4_pd_dmsdw.cmd.DmsdwCMD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DataSource dataSource;

    @Bean(name = "dmsdw2trinetx")
    DmsdwCMD dmsdw2trinetx(){
        return new DMSDW2TriNetXLiteCmd();
    }
}
