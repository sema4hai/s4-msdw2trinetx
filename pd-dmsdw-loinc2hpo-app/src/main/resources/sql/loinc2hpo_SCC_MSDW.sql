-- loinc2hpo for SCC labs in MSDW
CREATE TEMP TABLE denormalized_scc_lab_2020july as
WITH
	lab_scc_2020q2 as (
		SELECT l.medical_record_number , l.encounter_key , l.calendar_date , l.time_of_day_key , l.clinical_result_numeric ,  trim(from l.unit_of_measure_numeric) as unit_of_measure_numeric , l.reference_range , l.procedure_key,
		case when l.abnormal_flag is null then 'N' else l.abnormal_flag end as abnormal_flag
		FROM hai_az_prod.lab_scc_2020july l
		--where medical_record_number in (2966097, 3164683, 3869795)
	),
	lab_scc_abnormal_flag_mapping AS (
		SELECT case when abf.abnormal_flag is NULL then 'N' else abf.abnormal_flag end as abnormal_flag, abf.mapto
		FROM hai_az_prod.lab_scc_abnormal_flag_mapping abf
	)
SELECT lab.medical_record_number, lab.encounter_key , lab.calendar_date  as lab_date, dtod.clock_time_24_hour || ':00' as lab_time, lab.clinical_result_numeric , fdp.context_name, fdp.context_procedure_code AS local_test_code , loinc.loinc , lab.clinical_result_numeric AS lab_result_numeric_value, lab.unit_of_measure_numeric As unit_of_measure ,lab.reference_range AS normal_range , lab.abnormal_flag , abf.mapto AS interpretation, l2h.hpotermid , l2h.isnegated
FROM lab_scc_2020q2 lab
left JOIN hai_az_prod.fd_procedure fdp using (procedure_key)
left JOIN hai_az_prod.loinc_mapping loinc on fdp.context_procedure_code = loinc.code and (lab.unit_of_measure_numeric = loinc.unit or (lab.unit_of_measure_numeric is NULL and loinc.unit is NULL))
left JOIN lab_scc_abnormal_flag_mapping abf using (abnormal_flag)
left JOIN hai_az_prod.loinc2hpo l2h on loinc.loinc = l2h.loincid and abf.mapto = l2h.code
join prod_msdw.d_time_of_day dtod using (time_of_day_key);
