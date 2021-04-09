-- TODO: include newborn delivery
-- TODO: ~20% "canceled" billing records are also noted as "completed". Right now, I consider a billing record valid as long as it is noted "completed"
CREATE TABLE pd_prod_db.procedure_dmsdw_2020q2 AS
WITH procedure_for_staging AS (
SELECT DISTINCT dp.medical_record_number , f.encounter_key, f.caregiver_group_key, f.procedure_group_key,
	bp.procedure_key , bp.procedure_rank ,
	fdp.context_name , fdp.context_procedure_code , fdp.procedure_description , f.age_in_days_key , bp.procedure_role,
	dm.level1_context_name , dm.level2_event_name , dm.level3_action_name
	FROM dmsdw_2020q2.fact f
	JOIN dmsdw_2020q2.d_person dp on f.person_key = dp.person_key
	JOIN dmsdw_2020q2.b_procedure bp on f.procedure_group_key = bp.procedure_group_key
	JOIN dmsdw_2020q2.fd_procedure fdp on bp.procedure_key = fdp.procedure_key
	JOIN dmsdw_2020q2.d_metadata dm on f.meta_data_key = dm.meta_data_key
	where
	(procedure_role = 'Billing' and level1_context_name = 'EPIC' and level2_event_name = 'Encounter Order' and level3_action_name = 'Completed')
	OR (procedure_role in ('Primary', 'Principal', 'Secondary') and level1_context_name in ('EAGLE', 'TSI') and context_procedure_code != 'MSDW_UNKNOWN')
	OR (procedure_role = 'Primary' and level1_context_name = 'COMPURECORD' and level2_event_name = 'Anesthesia')
	OR (procedure_role = 'Surgical Procedure' and level1_context_name = 'HSM' and level3_action_name = 'Material Consumed')
	OR (procedure_role = 'Reported Hx')
order by f.person_key , f.age_in_days_key
)
SELECT DISTINCT medical_record_number, encounter_key, caregiver_group_key, procedure_group_key, procedure_key, procedure_rank,
context_name, context_procedure_code, procedure_description, age_in_days_key
FROM procedure_for_staging;