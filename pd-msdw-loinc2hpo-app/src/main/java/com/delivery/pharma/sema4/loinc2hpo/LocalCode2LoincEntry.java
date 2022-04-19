package com.delivery.pharma.sema4.loinc2hpo;

/**
 * Class to represent entries of local lab code to LOINC
 */
public class LocalCode2LoincEntry {

    private String sourceSystem;
    private String sourceCode;
    private String sourceTestName;
    private String unit;
    private String loinc;
    private String defaultUnit;


    public LocalCode2LoincEntry(String sourceSystem, String sourceCode, String sourceTestName, String unit, String loinc, String defaultUnit) {
        this.sourceSystem = sourceSystem;
        this.sourceCode = sourceCode;
        this.sourceTestName = sourceTestName;
        this.unit = unit;
        this.loinc = loinc;
        this.defaultUnit = defaultUnit;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceTestName() {
        return sourceTestName;
    }

    public void setSourceTestName(String sourceTestName) {
        this.sourceTestName = sourceTestName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getLoinc() {
        return loinc;
    }

    public void setLoinc(String loinc) {
        this.loinc = loinc;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    public void setDefaultUnit(String defaultUnit) {
        this.defaultUnit = defaultUnit;
    }
}
