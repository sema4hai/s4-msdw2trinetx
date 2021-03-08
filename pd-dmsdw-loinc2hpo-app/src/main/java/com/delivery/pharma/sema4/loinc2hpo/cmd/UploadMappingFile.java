package com.delivery.pharma.sema4.loinc2hpo.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    @Parameter(names = "--abnormalFlags", description = "specify abnormal flag mapping")
    private String abnormalFlagMappingPath;
    @Parameter(names = "--debug", description = "running in debug mode")
    private boolean debug = false;

    @Override
    public void run() {

        logger.info("loincMapping Path: " + labCode2LoincPath);
        logger.info("loinc2hpo path: " + loinc2HpoPath);
        logger.info("hpoTerms Path: " + hpoTermsPath);

        String schema = debug ? "pd_test_db" : "pd_prod_db";

        if (labCode2LoincPath != null){
            uploadLabCode2Loinc(labCode2LoincPath, jdbcTemplate, schema);
        }

        if (loinc2HpoPath != null) {
            uploadLoinc2Hpo(loinc2HpoPath, jdbcTemplate, schema);
        }

        if (hpoTermsPath != null) {
            uploadHpoTerms(hpoTermsPath, jdbcTemplate, schema);
        }

        if (abnormalFlagMappingPath != null){
            uploadAbnormalFlagMapping(abnormalFlagMappingPath, jdbcTemplate, schema);
        }
    }

    public void uploadLabCode2Loinc(String filePath, JdbcTemplate jdbcTemplate, String schema){
        jdbcTemplate.execute(String.format("DROP TABLE IF EXISTS %s.loinc_mapping", schema));
        String ddl = String.format("CREATE TABLE %s.loinc_mapping " +
                "(source VARCHAR(10)," +
                "code VARCHAR," +
                "test_name VARCHAR," +
                "unit VARCHAR," +
                "loinc VARCHAR," +
                "default_unit VARCHAR)", schema);
        jdbcTemplate.execute(ddl);

        String sql = String.format("INSERT INTO %s.loinc_mapping VALUES ", schema);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            // skip header line
            String line = reader.readLine();
            ArrayList<String> buffer = new ArrayList<>();
            while (true){
                line = reader.readLine();

                if (line == null){
                    if (!buffer.isEmpty()){
                        //flush buffer
                        String value = buffer.stream().map(s -> String.format("(%s)", s)).collect(Collectors.joining(","));
                        jdbcTemplate.execute(sql + value);
                        buffer.clear();
                    }
                    break;
                }

                //replace double quote with single quote
                if (line.contains("'")){
                    line = line.replaceAll("'", "''");
                }
                line = line.replaceAll("\"", "'")
                        .replaceAll("NA", "NULL");
                buffer.add(line);

                if (buffer.size() == 1000){
                    String value = buffer.stream().map(s -> String.format("(%s)", s)).collect(Collectors.joining(","));
                    jdbcTemplate.execute(sql + value);
                    buffer.clear();
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void uploadLoinc2Hpo(String filePath, JdbcTemplate jdbcTemplate, String schema){
        jdbcTemplate.execute(String.format("DROP TABLE IF EXISTS %s.loinc2hpo", schema));
        String ddl = String.format("CREATE TABLE %s.loinc2hpo (" +
                "loincId VARCHAR," +
                "loincScale VARCHAR," +
                "code_system VARCHAR," +
                "code VARCHAR," +
                "hpoTermId VARCHAR(10)," +
                "isNegated BOOLEAN)", schema);
        jdbcTemplate.execute(ddl);

        String sql = String.format("INSERT INTO %s.loinc2hpo values ", schema);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            String line = reader.readLine();
            ArrayList<Object[]> buffer = new ArrayList<>();
            while (true){
                line = reader.readLine();
                if (line == null){
                    if (!buffer.isEmpty()){
                        //flush buffer
                        String values = buffer.stream()
                                // add single quote to strings, and collapse into a single string separated by ,
                                .map(objs -> Arrays.stream(objs).map(e -> e instanceof Boolean ? e.toString() : "'" + e + "'").collect(Collectors.joining(",")))
                                // add parentheses to each loinc2hpo mapping entry
                                .map(valuesString -> "(" + valuesString + ")")
                                // join multiple entries together
                                .collect(Collectors.joining(","));
                        int n = jdbcTemplate.update(sql + values);
                        assert n == buffer.size();
                        buffer.clear();
                    }
                    break;
                }
                String[] fields = line.split("\t");
                String loincId = fields[0];
                String loincScale = fields[1];
                String system = fields[2];
                String code = fields[3];
                String hpoTermId = fields[4];
                boolean isNegated = (fields[5].equals("true"));
                buffer.add(new Object[]{loincId, loincScale, system, code, hpoTermId, isNegated});

                if (buffer.size() == 1000){
                    String values = buffer.stream()
                            // add single quote to strings, and collapse into a single string separated by ,
                            .map(objs -> Arrays.stream(objs).map(e -> e instanceof Boolean ? e.toString() : "'" + e + "'").collect(Collectors.joining(",")))
                            // add parentheses to each loinc2hpo mapping entry
                            .map(valuesString -> "(" + valuesString + ")")
                            // join multiple entries together
                            .collect(Collectors.joining(","));
                    int n = jdbcTemplate.update(sql + values);
                    assert n == 1000;
                    buffer.clear();
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public void uploadHpoTerms(String filePath, JdbcTemplate jdbcTemplate, String schema){
        jdbcTemplate.execute(String.format("DROP TABLE IF EXISTS %s.hpo", schema));
        String ddl = String.format("CREATE TABLE %s.hpo (termId VARCHAR(10), label VARCHAR)", schema);
        jdbcTemplate.execute(ddl);

        String sql = String.format("INSERT INTO %s.hpo values ", schema);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            // no header line
            String line;
            List<Object[]> buffer = new ArrayList<>();
            while(true){
                line = reader.readLine();
                //last line
                if (line == null){
                    //flush buffer
                    if (!buffer.isEmpty()){
                        String values = buffer.stream().map(objs -> String.format("('%s', '%s')", objs[0], objs[1]))
                                .collect(Collectors.joining(","));
                        jdbcTemplate.update(sql + values + ";");
                        buffer.clear();
                    }
                    break;
                }
                String[] fields = line.split(",", 2);
                //escape single quote: replace with double quote (Redshift specific)
                buffer.add(new Object[]{fields[0], fields[1].replaceAll("'", "''")});
                if (buffer.size() == 1000){
                    String values = buffer.stream().map(objs -> String.format("('%s', '%s')", objs[0], objs[1]))
                            .collect(Collectors.joining(","));
                    jdbcTemplate.update(sql + values + ";");
                    buffer.clear();
                    logger.trace("new batch updated");
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void uploadAbnormalFlagMapping(String filePath, JdbcTemplate jdbcTemplate, String schema){
        jdbcTemplate.execute(String.format("DROP TABLE IF EXISTS %s.lab_scc_abnormal_flag_mapping", schema));
        String ddl = String.format("CREATE TABLE %s.lab_scc_abnormal_flag_mapping (" +
                "abnormal_flag VARCHAR, " +
                "mapTo VARCHAR)", schema);
        jdbcTemplate.execute(ddl);

        String sql = String.format("INSERT INTO %s.lab_scc_abnormal_flag_mapping values ", schema);
        ArrayList<String> values = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            String line = reader.readLine();
            while ((line = reader.readLine()) != null){
                String[] fields = line.split(",");
                String abnormal_flag = fields[0];
                String map_to = fields[2];
                if (abnormal_flag.equals("NULL")){
                    values.add(String.format("(NULL, '%s')", map_to));
                } else {
                    values.add(String.format("('%s', '%s')", abnormal_flag, map_to));
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        jdbcTemplate.update(sql + StringUtils.join(values, ","));
    }

}
