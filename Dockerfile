FROM openjdk:11

RUN mkdir /s4-msdw2trinetx

COPY pd-dmsdw-cmd-app/target/original-pd-dmsdw-cmd-app-0.0.1-SNAPSHOT.jar /s4-msdw2trinetx

COPY pd-msdw-cmd-app/target/original-pd-msdw-cmd-app-0.0.1-SNAPSHOT.jar /s4-msdw2trinetx

COPY pd-loinc2hpo-app/target/original-pd-loinc2hpo-app-0.0.1-SNAPSHOT.jar /s4-msdw2trinetx

WORKDIR /s4-msdw2trinetx

CMD ["bash"]