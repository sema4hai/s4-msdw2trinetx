CREATE TABLE hai_az_prod.scc_and_epic_lab_after_loinc2hpo_2020july AS
WITH epic_denormalized AS (
  SELECT medical_record_number, lab_date, lab_time, context_name, local_test_code :: VARCHAR, loinc,  lab_result_numeric_value, unit_of_measure, normal_range, interpretation, hpotermid, isnegated
  FROM denormalized_epic_lab_2020july
),
scc_denormalized AS (
  SELECT medical_record_number, lab_date, lab_time, context_name, local_test_code, loinc, lab_result_numeric_value, unit_of_measure, normal_range, interpretation, hpotermid, isnegated
  FROM denormalized_scc_lab_2020july
)
SELECT * FROM scc_denormalized
UNION all
SELECT * FROM epic_denormalized;