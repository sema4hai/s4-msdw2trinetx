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
    @Parameter(names = "--hpoIsAPairs", description = "HPO is_a pairs, along with their distance as a flat file")
    private String hpoIsAPairPath;
    @Parameter(names = "--abnormalFlags", description = "specify abnormal flag mapping")
    private String abnormalFlagMappingPath;
    @Parameter(names = "--race", description = "specify race map")
    private String raceMapPath;
    @Parameter(names = "--biome", description = "specify biome sample-mrn map")
    private String biomePath;
    @Parameter(names = "--rxnorm", description = "specify rxnorm map")
    private String rxnormPath;
    @Parameter(names = "--debug", description = "running in debug mode")
    private boolean debug = false;
    @Parameter(names = "--schema", description = "explicitly specify the schema")
    private String schema;

    @Override
    public void run() {

        logger.info("loincMapping Path: " + labCode2LoincPath);
        logger.info("loinc2hpo path: " + loinc2HpoPath);
        logger.info("hpoTerms Path: " + hpoTermsPath);
        logger.info("hpoIsAPairPath: " + hpoIsAPairPath);
        logger.info("race_map Path: " + raceMapPath);

        if (schema == null){
            schema = debug ? "pd_test_db" : "pd_prod_db";
        }

        if (labCode2LoincPath != null){
            uploadLabCode2Loinc(labCode2LoincPath, jdbcTemplate, schema);
        }

        if (loinc2HpoPath != null) {
            uploadLoinc2Hpo(loinc2HpoPath, jdbcTemplate, schema);
        }

        if (hpoTermsPath != null) {
            uploadHpoTerms(hpoTermsPath, jdbcTemplate, schema);
        }

        if (hpoIsAPairPath != null){
            uploadHpoIsAPairs(hpoIsAPairPath, jdbcTemplate, schema);
        }

        if (abnormalFlagMappingPath != null){
            uploadAbnormalFlagMapping(abnormalFlagMappingPath, jdbcTemplate, schema);
        }

        if (raceMapPath != null){
            uploadRaceMapping(raceMapPath, jdbcTemplate, schema);
        }

        if (biomePath != null){
            uploadBiomeMapping(biomePath, jdbcTemplate, schema);
        }

        if (rxnormPath != null){
            uploadRxNormMapping(rxnormPath, jdbcTemplate, schema);
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
        String ddl = String.format("CREATE TABLE %s.hpo (termId VARCHAR(10), distanceToRoot INTEGER, label VARCHAR)", schema);
        jdbcTemplate.execute(ddl);

        String sql = String.format("INSERT INTO %s.hpo values ", schema);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            // no header line
            String line;
            int line_count = 0;
            List<Object[]> buffer = new ArrayList<>();
            while(true){
                line_count++;
                line = reader.readLine();
                //last line
                if (line == null){
                    //flush buffer
                    if (!buffer.isEmpty()){
                        String values = buffer.stream().map(objs -> String.format("('%s', %s, '%s')", objs[0], objs[1], objs[2]))
                                .collect(Collectors.joining(","));
                        jdbcTemplate.update(sql + values + ";");
                        buffer.clear();
                    }
                    break;
                }
                String[] fields = line.split(",",3);
                if (fields.length != 3){
                    System.err.println(String.format("line %d does not have 3 fields: ", line_count));
                    System.out.println(line);
                    throw new RuntimeException();
                }
                //escape single quote: replace with double quote (Redshift specific)
                buffer.add(new Object[]{fields[0], fields[1], fields[2].replaceAll("'", "''")});
                if (buffer.size() == 1000){
                    String values = buffer.stream().map(objs -> String.format("('%s', %s, '%s')", objs[0], objs[1], objs[2]))
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

    public void uploadHpoIsAPairs(String filePath, JdbcTemplate jdbcTemplate, String schema){
        jdbcTemplate.execute(String.format("DROP TABLE IF EXISTS %s.hpo_is_a_pairs", schema));
        String ddl = String.format("CREATE TABLE %s.hpo_is_a_pairs (current VARCHAR(10), ancestor VARCHAR(10), distance INTEGER)", schema);
        jdbcTemplate.execute(ddl);

        String sql = String.format("INSERT INTO %s.hpo_is_a_pairs values ", schema);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            // no header line
            String line = reader.readLine();
            List<Object[]> buffer = new ArrayList<>();
            while(true){
                line = reader.readLine();
                //last line
                if (line == null){
                    //flush buffer
                    if (!buffer.isEmpty()){
                        String values = buffer.stream().map(objs -> String.format("('%s', '%s', %s)", objs[0], objs[1], objs[2]))
                                .collect(Collectors.joining(","));
                        jdbcTemplate.update(sql + values + ";");
                        buffer.clear();
                        logger.trace("last batch updated");
                    }
                    break;
                }
                String[] fields = line.split(",", 3);
                //escape single quote: replace with double quote (Redshift specific)
                buffer.add(new Object[]{fields[0], fields[1], fields[2]});
                if (buffer.size() == 10000){
                    String values = buffer.stream().map(objs -> String.format("('%s', '%s', %s)", objs[0], objs[1], objs[2]))
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

    public void uploadRaceMapping(String filePath, JdbcTemplate jdbcTemplate, String schema){

        jdbcTemplate.execute(String.format("DROP TABLE IF EXISTS %s.dmsdw_race_map", schema));
        String ddl = String.format("CREATE TABLE %s.dmsdw_race_map (" +
                "race VARCHAR, " +
                "mapTo VARCHAR)", schema);
        jdbcTemplate.execute(ddl);

        String sql = String.format("INSERT INTO %s.dmsdw_race_map values ", schema);
        ArrayList<String> values = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            String line = reader.readLine();
            while ((line = reader.readLine()) != null){
                String[] fields = line.split(",");
                String race = fields[0];
                String map_to = fields[2];
                if (race.equals("")){
                    values.add(String.format("(NULL, '%s')", map_to));
                } else {
                    values.add(String.format("('%s', '%s')", race, map_to));
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        jdbcTemplate.update(sql + StringUtils.join(values, ","));

    }

    public void uploadBiomeMapping(String filePath, JdbcTemplate jdbcTemplate, String schema){

        jdbcTemplate.execute(String.format("DROP TABLE IF EXISTS %s.biome_sample_mrn_map", schema));
        String ddl = String.format("CREATE TABLE %s.biome_sample_mrn_map (" +
                "mt_sinai_patient_id VARCHAR, " +
                "mt_sinai_sample_id VARCHAR, " +
                "mrn_de_id_link VARCHAR," +
                "s4_sample_id VARCHAR)", schema);
        jdbcTemplate.execute(ddl);

        String sql = String.format("INSERT INTO %s.biome_sample_mrn_map values ", schema);
        ArrayList<String> values = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            String line = reader.readLine();
            while ((line = reader.readLine()) != null){
                String[] fields = line.split(",", 4);
                if (fields.length != 4){
                    System.out.printf("length of line is %s%n", fields.length);
                    System.out.println(line);
                }
                String sinai_patient_id = fields[0];
                String sinai_sample_id = fields[1];
                String mrn_deid = fields[2];
                String s4_sample_id = fields[3];
                if (mrn_deid.equals("NULL")){
                    values.add(String.format("('%s', '%s', NULL, '%s')", sinai_patient_id, sinai_sample_id, s4_sample_id));
                    //System.out.printf("ill formatted mrn_deid: %s%n", line);
                    //System.out.println(String.format("('%s', '%s', NULL, '%s')", sinai_patient_id, sinai_sample_id, s4_sample_id));
                } else if (s4_sample_id.equals("") | s4_sample_id.equals("#N/A")){
                    values.add(String.format("('%s', '%s', '%s', NULL)", sinai_patient_id, sinai_sample_id, mrn_deid));
                    //System.out.printf("ill formated s4_sample_id: %s%n", line);
                    //System.out.println(String.format("('%s', '%s', '%s', NULL)", sinai_patient_id, sinai_sample_id, mrn_deid));
                } else {
                    values.add(String.format("('%s', '%s', '%s', '%s')", sinai_patient_id, sinai_sample_id, mrn_deid, s4_sample_id));
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        //System.out.println(sql + StringUtils.join(values, ","));
        jdbcTemplate.update(sql + StringUtils.join(values, ","));

    }


    public void uploadRxNormMapping(String filePath, JdbcTemplate jdbcTemplate, String schema){

        jdbcTemplate.execute(String.format("DROP TABLE IF EXISTS %s.rxnorm_map", schema));
        String ddl = String.format("CREATE TABLE %s.rxnorm_map (" +
                "context_name VARCHAR, " +
                "context_material_code VARCHAR, " +
                "n INTEGER, " +
                "material_name VARCHAR, " +
                "queryText VARCHAR, " +
                "rxnorm VARCHAR)", schema);
        jdbcTemplate.execute(ddl);

        String sql = String.format("INSERT INTO %s.rxnorm_map values ", schema);
        ArrayList<String> values = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            String line = reader.readLine();
            while ((line = reader.readLine()) != null){
                String[] fields = line.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", 6);
                if (fields.length != 6){
                    System.out.printf("length of line is %s%n", fields.length);
                    System.out.println(line);
                }
                String context_name = fields[0];
                String context_material_code = fields[1].replaceAll("'", "''");
                int n = Integer.parseInt(fields[2]);
                String material_name = fields[3].replaceAll("\"", "").replaceAll("'", "''");
                String queryText = fields[4].replaceAll("'", "''");
                String rxnorm = fields[5];
                if (rxnorm.equals("")){
                    values.add(String.format("('%s', '%s', %d, '%s', '%s', NULL)", context_name, context_material_code, n, material_name, queryText));
//                    System.out.printf("not mapped to rxnorm: %s%n", line);
//                    System.out.println(String.format("('%s', '%s', %d, '%s', '%s', NULL)", context_name, context_material_code, n, material_name, queryText));
                } else {
                    values.add(String.format("('%s', '%s', %d, '%s', '%s', '%s')", context_name, context_material_code, n, material_name, queryText, rxnorm));
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
