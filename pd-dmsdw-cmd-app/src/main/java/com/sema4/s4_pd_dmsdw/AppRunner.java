package com.sema4.s4_pd_dmsdw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class AppRunner implements CommandLineRunner {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        Integer personCount = jdbcTemplate.queryForObject("SELECT count(*) FROM dmsdw_2019q1.d_person;", Integer.class);
        System.out.println("Person count: " + personCount);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS pd_test_db.test_table (id INT, name VARCHAR(10));");
        jdbcTemplate.execute("INSERT INTO pd_test_db.test_table VALUES (1, 'Aaron')");
        jdbcTemplate.execute("INSERT INTO pd_test_db.test_table VALUES (2, 'Kelly')");
        List<Person> personList = jdbcTemplate.query("SELECT * FROM pd_test_db.test_table", new RowMapper<Person>() {
            @Override
            public Person mapRow(ResultSet resultSet, int i) throws SQLException {
                return new Person(resultSet.getInt(1), resultSet.getString(2));
            }
        });
        jdbcTemplate.execute("DROP TABLE pd_test_db.test_table");
        personList.forEach(System.out::println);
    }

    static class Person {
        private int id;
        private String name;

        public Person(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
