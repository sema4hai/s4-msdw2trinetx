package com.delivery.pharma.sema4.msdw.command;

import com.beust.jcommander.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Parameters(commandDescription = "Transform MSDW into a model similar to TriNetX")
public class MSDW2TriNetXLite implements Command {

    @Autowired
    DataSource dataSource;

    @Override
    public void run() {
        Resource preparation = new ClassPathResource("sql/preparation.sql");
        Resource person = new ClassPathResource("sql/person.sql");
        Resource diagnosis = new ClassPathResource("sql/diagnosis.sql");
        Resource lab_epic = new ClassPathResource("sql/lab_epic.sql");
        Resource lab_scc = new ClassPathResource("sql/lab_scc.sql");
        Resource medication = new ClassPathResource("sql/medication.sql");
        Resource procedure = new ClassPathResource("sql/procedure.sql");

        // ref https://stackoverflow.com/questions/30732314/execute-sql-file-from-spring-jdbc-template
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScripts(preparation, person, diagnosis, lab_epic, lab_scc, medication, procedure);
//        databasePopulator.execute(dataSource);

        System.out.println(databasePopulator);
//        System.out.println(resource.getFilename());
//        try (BufferedReader reader = new BufferedReader(new FileReader(resource.getFile()))){
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }
}
