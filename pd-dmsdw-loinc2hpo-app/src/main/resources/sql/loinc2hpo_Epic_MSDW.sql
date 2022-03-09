-- loinc2hpo for EPIC labs in MSDW
-- have to use distinct because it appears each record is duplicated twice
create temp table denormalized_epic_lab_2020july as
with interpreted as (
	SELECT distinct lab.mrn, lab.order_date , lab.lab_time , lab.test_code , lab.test_result_value , lab.unit_of_measurement , lab.reference_range ,
	case
			when test_result_value_final :: REAL < range_low_final ::REAL then 'L'
			when test_result_value_final :: REAL > range_hi_final :: REAL then 'H'
			else 'N'
	end as interpretation
	FROM hai_az_test.lab_epic_2020july lab
	--where mrn in (3710567, 8014250, 3144637)
	)
SELECT lab.mrn AS medical_record_number, NULL AS encounter_key, lab.order_date as lab_date, lab.lab_time, 'EPIC' AS context_name, lab.test_code AS local_test_code , local2loinc.loinc , lab.test_result_value AS lab_result_numeric_value, lab.unit_of_measurement AS unit_of_measure , lab.reference_range AS normal_range , NULL AS abnormal_flag,  interpretation,
loinc2hpo.hpotermid , loinc2hpo.isnegated
FROM interpreted lab
LEFT JOIN hai_az_prod.loinc_mapping local2loinc on lab.test_code :: VARCHAR = local2loinc.code and (lab.unit_of_measurement = local2loinc.unit OR (lab.unit_of_measurement is NULL and local2loinc.unit is NULL))
LEFT JOIN hai_az_prod.loinc2hpo loinc2hpo on local2loinc.loinc = loinc2hpo.loincid and interpretation = loinc2hpo.code
where local2loinc."source" = 'EPIC';