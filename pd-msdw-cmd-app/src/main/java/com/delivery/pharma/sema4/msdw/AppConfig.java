package com.delivery.pharma.sema4.msdw;

import com.delivery.pharma.sema4.msdw.command.Command;
import com.delivery.pharma.sema4.msdw.command.MSDW2TriNetXLite;
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

    @Bean(name = "msdw2trinetx")
    public Command msdw2trinetx(){
        return new MSDW2TriNetXLite();
    }
}
