# s4-msdw2trinetx

This repo contains three major applications, 

- pd-msdw-cmd-app: a command line app to transform MSDW EHR into a TriNetX-LITE CDM
- pd-loinc2hpo-app: a command line app for LOINC2HPO transformation  (after transforming to the TriNetX-LITE CDM; for MSDW only)
- pd-dmsdw-cmd-app: a command line app to transform DMSDW into a TriNetX-LITE CDM

\* What does "TriNetX-LITE" mean? It basically means the resulting data is very close to but not exactly TriNetX CDM. 

Below is a description of each app. 

## pd-msdw-cmd-app

This app transforms the MSDW EHR data into a TriNetX-like common data model. 

The SQL queries used for data transformation is under the [sql](./pd-msdw-cmd-app/src/main/resources/sql) folder. You can further modify the queries if you think the logic can be improved. But generally you should not run the SQL queries manually for production. Instead, call the main function in ```MSDW2TriNetX_LITE_APP```, which will run the queries in the sequence defined in ```MSDW2TriNetXLiteCmd```. 

## pd-dmsdw-cmd-app

This app transforms the now deprecated DMSDW EHR data into a TriNetX-like common data model. It was developed before pd-msdw-cmd-app, and the latter was actually adapted from it (therefore they are nearly identical, except some customizations for ETL SQL queries).


## pd-loinc2hpo-app

This app is an implementation of the LOINC2HPO algorithm. MSDW EHR data has to be converted into TriNetX with ```pd-msdw-cmd-app``` first before you can use this app. It currently only supports MSDW but can be extended to DMSDW if necessary.

Implementation of LOINC2HPO can be roughly divided into two steps:

Step 1, convert lab results from local code to LOINC;

Step 2, convert LOINC-coded lab results into HPO with the published LOINC2HPO algorithm ([paper link](https://www.nature.com/articles/s41746-019-0110-4)). The linked paper will point to you where to find the loinc2hpoAnnotation file (it is also cached under the [resource](./pd-loinc2hpo-app/src/main/resources) directory for convenience), and how the algorithm works. 

For Step 1, one can find the mapping file from local test code to LOINC under the [resource](./pd-loinc2hpo-app/src/main/resources) folder. The same folder also contains other resources, e.g. HPO list, HPO pairs etc, that are required for implementing this algorithm. These resource files are uploaded to the Redshift cluster by the [UploadMappingFile](./pd-loinc2hpo-app/src/main/java/com/delivery/pharma/sema4/loinc2hpo/cmd/UploadMappingFile.java) class. 

For Step 2, one can find the ETL queries under the [resource/sql](./pd-loinc2hpo-app/src/main/resources/sql) folder. You should let the app run the queries for you instead of running them manually. The app uses the [Loinc2HpoCmd](./pd-loinc2hpo-app/src/main/java/com/delivery/pharma/sema4/loinc2hpo/cmd/Loinc2HpoCmd.java) class to run the queries. 


# How to use the apps?

Option 1, you can always use an IDE (IntelliJ IDEA) to run them, especially if you are also developing them. 

Option 2, package the code into jar files and run them from the terminal. 

Regardless of which option you choose, you will need to provide database credentials to DSCA with the following environment variables:

- spring.datasource.url
- spring.datasource.username
- spring.datasource.password

Refer to the ```application.properties``` files under the resource directory of each app to find default values, for example, [application-prod.properties](./pd-msdw-cmd-app/src/main/resources/application-prod.properties).

Here is a step-by-step process for Option 2:

1. Package jar files. Within this code directory, run
    ```bash 
    # you need to have maven installed, refer to https://maven.apache.org/install.html
    mvn package
    ```
   This will create jar files (starting with "original") in the target folder under each application folder.

2. Create a docker image to run the apps. The base image is bundled with a Java 11 JRE. Skip Step 2 and 3 if you want to run the jar files directly on your laptop (you need a Java 11 runtime or higher).
    ``` 
    docker build --tag msdw2trinetx:0.1 .
    ```
   The resulting image has all three jar files under the ```/s4-msdw2trinetx``` directory.

3. Start a docker container. 
    ```
    # start your docker and in your terminal
    docker run -it msdw2trinetx:0.1
       
    # within the docker container
    # you may run the ls command to list the jar files: you should see three jar files
    ```
4. Run the jar file for ```pd-msdw-cmd-app``` to convert MSDW EHR into a TriNetX CDM. 
    ```
   # run any jar file and it will produce a help message
   java -jar original-pd-msdw-cmd-app-0.0.1-SNAPSHOT.jar
    ```
    Hopefully you see outputs like below:
    ```
    root@b2aaa2b19a92:/s4-msdw2trinetx# java -jar original-pd-msdw-cmd-app-0.0.1-SNAPSHOT.jar 
    
      .   ____          _            __ _ _
     /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
    ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
     \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
      '  |____| .__|_| |_|_| |_\__, | / / / /
     =========|_|==============|___/=/_/_/_/
     :: Spring Boot ::        (v2.1.4.RELEASE)
    
    2022-04-20 13:32:00.216  INFO 8 --- [           main] c.d.p.sema4.msdw.MSDW2TriNetX_LITE_APP   : Starting MSDW2TriNetX_LITE_APP v0.0.1-SNAPSHOT on b2aaa2b19a92 with PID 8 (/s4-msdw2trinetx/original-pd-msdw-cmd-app-0.0.1-SNAPSHOT.jar started by root in /s4-msdw2trinetx)
    2022-04-20 13:32:00.219  INFO 8 --- [           main] c.d.p.sema4.msdw.MSDW2TriNetX_LITE_APP   : The following profiles are active: prod
    2022-04-20 13:32:01.196  INFO 8 --- [           main] c.d.p.sema4.msdw.MSDW2TriNetX_LITE_APP   : Started MSDW2TriNetX_LITE_APP in 1.418 seconds (JVM running for 1.821)
    Start Sema4MSDW2TriNetX-Lite CliRunner...
    Usage: <main class> [command] [command options]
      Commands:
        msdw2trinetx      Transform MSDW into a model similar to TriNetX
          Usage: msdw2trinetx
    ```

    To actually run the command, run below after replacing the variables with your information (remember to remove curly brackets)
    ```
    java -jar original-pd-msdw-cmd-app-0.0.1-SNAPSHOT.jar msdw2trinetx \
    spring.datasource.url=jdbc:redshift://{localhost:2345}/dev?useUnicode\=true&useJDBCCompliantTimezoneShift\=true&useLegacyDatetimeCode\=false&serverTimezone\=UTC&rewriteBatchedStatements\=true
    spring.datasource.username={user}
    spring.datasource.password={password}
    ```
    If you are running it from your local computer, you probably have to set up port forwarding first (refer to the documentation [here](https://github.com/sema4hai/s4-biopharma-collaborate-archetype)). If you are running it within docker, your host should be "host.docker.internal" instead of "localhost". ```usr``` and ```password``` are your database credentials.

    It should take a while (more than half an hour) for it to complete. When it is done you should get MSDW EHR in the TriNetX format.
5. After completing Step 4, run the jar file for ```pd-loinc2hpo-app``` for LOINC2HPO transformation. 
    ```
    java -jar original-pd-loinc2hpo-app-0.0.1-SNAPSHOT.jar
    ```
   You should get the usage information in your terminal like below. There are many commands, but you only need to run ```uploadMappingFile``` and ```loinc2hpo``` successively for this task.
    ```
    Start Sema4Loinc2HpoCliRunner...
    Usage: <main class> [command] [command options]
      Commands:
        termMap      Generate HPO term map file
          Usage: termMap [options]
            Options:
            * --hpo
    
              -o, --output
                filepath for results
    
        isAPairs      Generate HPO term pairs with an is_a relationship
          Usage: isAPairs [options]
            Options:
            * --hpo
    
              -o, --output
                filepath for results
    
        uploadMappingFile      Upload mapping files to DMSDW for analysis
          Usage: uploadMappingFile [options]
            Options:
              --abnormalFlags
                specify abnormal flag mapping
              --biome
                specify biome sample-mrn map
              --debug
                running in debug mode
                Default: false
              --hpoIsAPairs
                HPO is_a pairs, along with their distance as a flat file
              --hpoTerms
                HPO term list as a flat file
              --labCode2Loinc
                specify the mapping file from local code to LOINC
              --loinc2Hpo
                specify the mapping file from LOINC to HPO
              --race
                specify race map
              --rxnorm
                specify rxnorm map
              --schema
                explicitly specify the schema
    
        inferHpo      Infer ancestor HPO terms
          Usage: inferHpo [options]
            Options:
            * --hpo
    
              -i, --input
                lab result, must have loinc, interpretation
              -o, --output
                filepath for results
              --sep
                column separator
                Default: ,(?=(?:[^"]*"[^"]*")*[^"]*$)
    
        loincTestablePhenotypes      Create a table to indicate which phenotypes 
                can be tested from each LOINC
          Usage: loincTestablePhenotypes
    
        loinc2hpo      Transform lab tests into HPO terms
          Usage: loinc2hpo [options]
            Options:
            * --schema
    ```

    The following shows how to run ```uploadMappingFile``` (note it is not a valid bash command due to line breaks and comments; all required files are contained in the [resource](./pd-loinc2hpo-app/src/main/resources) directory except for ```race``` mapping):
    ```
    java -jar original-pd-loinc2hpo-app-0.0.1-SNAPSHOT.jar
    uploadMappingFile
    # you may specify a schema that you have write access to
    --schema
    hai_az_prod
    --loinc2Hpo
    {PATH TO}/loinc2hpoAnnotations_v2.0.tsv
    --hpoTerms
    {PATH TO}/hp_term_list.csv
    --hpoIsAPairs
    {PATH TO}/hp_is_a_pairs.csv
    --race
    # download race mapping from here https://github.com/sema4genomics/s4-pd-pharma-query/blob/master/src/main/python/resource/dmsdw_race_map.csv
    # the race mapping works for both dmsdw and msdw
    {PATH TO}/dmsdw_race_map.csv
    --labCode2Loinc
    {PATH TO}/loinc_mapping_v3.0_msdw.csv
    --abnormalFlags
    {PATH TO}/lab_scc_abnormal_flag_normalization.csv
    ```
   
    The following shows how to run ```loinc2hpo``` command. 
    ```
    java -jar original-pd-loinc2hpo-app-0.0.1-SNAPSHOT.jar loinc2hpo --schema MSDW
    ```
