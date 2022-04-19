package com.delivery.pharma.sema4.loinc2hpo.cmd;

import com.beust.jcommander.Parameters;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Parameters(commandDescription = "Create a table to indicate which phenotypes can be tested from each LOINC")
public class LoincTestablePhenotypesCmd implements Command {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run() {
        String sql;
        try {
            List<String> lines = Files.readAllLines(Paths.get(new ClassPathResource("sql/loinc_testable_phenotypes.sql").getURI()), Charset.defaultCharset());
            sql = StringUtils.join(lines, "\n");
            System.out.println(sql);
            // TODO: enable this
//            jdbcTemplate.execute(sql);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
