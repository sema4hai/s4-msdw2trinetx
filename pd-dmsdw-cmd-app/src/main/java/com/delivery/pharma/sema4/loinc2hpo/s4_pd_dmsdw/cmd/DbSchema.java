package com.delivery.pharma.sema4.loinc2hpo.s4_pd_dmsdw.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class DbSchema implements DmsdwCMD {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run() {

    }

    private List<Table> allTables(){

        String sql = "SELECT schemaname, tablename, tableowner FROM pg_tables where schemaname = 'dmsdw_2019q1';";
        List<Table> tables = jdbcTemplate.queryForList(sql, Table.class);
        return tables;

    }

    private List<Column> allColumns(){

        String sql = "SELECT table_schema, table_name, column_name, ordinal_position, is_nullable, data_type " +
                "from information_schema.columns " +
                "where table_schema ~ 'dmsdw_2019q1' order by table_name, ordinal_position ;";
        List<Column> columns = jdbcTemplate.queryForList(sql, Column.class);
        return columns;

    }

    private int recordCountsOf(String schemaname, String tableName){

        String sql = String.format("SELECT COUNT(*) FROM %s.%s;", schemaname, tableName);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count;

    }

    private int uniqueValueCounts(String schemaname, String tablename, String columnname){
        String sql = String.format("SELECT COUNT(DISTINCT(%s)) FROM %s.%s;", columnname, schemaname, tablename);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count;

    }

    private List<String> uniqueColumnValues(String schemaname, String tablename, String columnname, int uniqueValueCounts){

        int limit = Math.min(10, uniqueValueCounts);
        String sql = String.format("SELECT DISTINCT(%s) FROM %s.%s LIMIT %d;", columnname, schemaname, tablename, limit);
        List<String> uniqueValues = jdbcTemplate.queryForList(sql, String.class);
        return uniqueValues;

    }

    protected static class Table {
        private String schemaname;
        private String tablename;
        private String tableowner;

        public Table(String schemaname, String tablename, String tableowner) {
            this.schemaname = schemaname;
            this.tablename = tablename;
            this.tableowner = tableowner;
        }

        public String getSchemaname() {
            return schemaname;
        }

        public void setSchemaname(String schemaname) {
            this.schemaname = schemaname;
        }

        public String getTablename() {
            return tablename;
        }

        public void setTablename(String tablename) {
            this.tablename = tablename;
        }

        public String getTableowner() {
            return tableowner;
        }

        public void setTableowner(String tableowner) {
            this.tableowner = tableowner;
        }
    }

    protected static class Column {
        private String table_schema;
        private String table_name;
        private String column_name;
        private int ordinal_position;
        private String is_nullable;
        private String data_type;

        public Column(String table_schema, String table_name, String column_name, int ordinal_position, String is_nullable, String data_type) {
            this.table_schema = table_schema;
            this.table_name = table_name;
            this.column_name = column_name;
            this.ordinal_position = ordinal_position;
            this.is_nullable = is_nullable;
            this.data_type = data_type;
        }

        public String getTable_schema() {
            return table_schema;
        }

        public void setTable_schema(String table_schema) {
            this.table_schema = table_schema;
        }

        public String getTable_name() {
            return table_name;
        }

        public void setTable_name(String table_name) {
            this.table_name = table_name;
        }

        public String getColumn_name() {
            return column_name;
        }

        public void setColumn_name(String column_name) {
            this.column_name = column_name;
        }

        public int getOrdinal_position() {
            return ordinal_position;
        }

        public void setOrdinal_position(int ordinal_position) {
            this.ordinal_position = ordinal_position;
        }

        public String getIs_nullable() {
            return is_nullable;
        }

        public void setIs_nullable(String is_nullable) {
            this.is_nullable = is_nullable;
        }

        public String getData_type() {
            return data_type;
        }

        public void setData_type(String data_type) {
            this.data_type = data_type;
        }
    }






}
