# s4-msdw2trinetx

This repo contains three majoy applications, 

- pd-msdw-cmd-app: a command line app to transform MSDW into a TriNetX-like CDM
- pd-msdw-loinc2hpo-app: a command line app for LOINC2HPO transformation for MSDW (after transforming to the TriNetX-like CDM)
- pd-dmsdw-cmd-app: a command line app to transform DMSDW into a TriNetX-like CDM

Below is a description of each app. 

## pd-msdw-cmd-app

This app transforms the MSDW EHR data into a TriNetX-like common data model. 

TODO: more details

## pd-msdw-loinc2hpo-app

After converting MSDW EHR data into TriNetX with the pd-msdw-cmd-app, this app implements LOINC2HPO, i.e. transform lab tests into HPO. 

TODO: more details

## pd-dmsdw-cmd-app

This app transforms the now deprecated DMSDW EHR data into a TriNetX-like common data model. It was developed before pd-msdw-cmd-app, and is actually the backbone for the latter (therefore they are nearly identical, except some customizations for ETL SQL queries).

TODO: more details