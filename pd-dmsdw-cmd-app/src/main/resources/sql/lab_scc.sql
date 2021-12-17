CREATE TABLE pd_prod_db.lab_scc_2020q4 AS
with
	lab_flat as (
		SELECT dp.medical_record_number , f.person_key , f.encounter_key , f.caregiver_group_key , f.facility_key , f.operation_key, f.age_in_days_key, f.time_of_day_key,
		bp.procedure_group_key , bp.procedure_rank , bp.procedure_key , fdp.context_name , fdp.context_procedure_code , fdp.procedure_description ,
		dm.level1_context_name , dm.level2_event_name , dm.level3_action_name ,
		LISTAGG(case when dm.level4_field_name = 'Abnormal Flag' then f.value end, '|') as Abnormal_Flag,
		LISTAGG(case when dm.level4_field_name = 'Clinical Result Numeric' then f.value end, '|') as Clinical_Result_Numeric,
		LISTAGG(case when dm.level4_field_name = 'Clinical Result String' then f.value end, '|') as Clinical_Result_String,
		LISTAGG(case when dm.level4_field_name = 'Reference Range' then f.value end, '|') as Reference_Range,
		LISTAGG(case when dm.level4_field_name = 'Clinical Result Text[01]' then f.value end, '|') as Clinical_Result_Text_01,
		LISTAGG(case when dm.level4_field_name = 'Comments' then f.value end, '|') as Comments,
		LISTAGG(case when dm.level4_field_name = 'Filler Order Number' then f.value end, '|') as Filler_Order_Number,
		LISTAGG(case when dm.level4_field_name = 'Placer Group Number' then f.value end, '|') as Placer_Group_Number,
		LISTAGG(case when dm.level4_field_name = 'Placer Order Number' then f.value end, '|') as Placer_Order_Number,
		LISTAGG(case when dm.level4_field_name = 'Research Indicator' then f.value end, '|') as Research_Indicator,
		LISTAGG(case when dm.level4_field_name = 'Specimen Source' then f.value end, '|') as Specimen_Source,
		LISTAGG(case when dm.level4_field_name = 'Clinical Result Numeric' then duom.unit_of_measure end, '|' ) as unit_of_measure_numeric,
		LISTAGG(case when dm.level4_field_name = 'Clinical Result String' then duom.unit_of_measure end, '|' ) as unit_of_measure_string,
		LISTAGG(case when dm.level4_field_name = 'Reference Range' then duom.unit_of_measure end, '|' ) as unit_of_measure_reference_range
		FROM dmsdw_2020q4.fact_lab f
		JOIN dmsdw_2020q4.d_person dp on f.person_key = dp.person_key
		JOIN dmsdw_2020q4.b_procedure bp on f.procedure_group_key = bp.procedure_group_key
		JOIN pd_prod_db.fd_procedure fdp on bp.procedure_key = fdp.procedure_key
		JOIN dmsdw_2020q4.d_metadata dm on f.meta_data_key = dm.meta_data_key
		JOIN dmsdw_2020q4.d_unit_of_measure duom on f.uom_key = duom.uom_key
		where bp.procedure_role = 'Result' and level1_context_name = 'SCC' and level2_event_name = 'Lab Test' and level3_action_name = 'Final Result'
		GROUP BY dp.medical_record_number , f.person_key , f.encounter_key , f.caregiver_group_key , f.facility_key , f.operation_key, f.age_in_days_key, f.time_of_day_key,
		bp.procedure_group_key , bp.procedure_rank , bp.procedure_key , fdp.context_name , fdp.context_procedure_code , fdp.procedure_description ,
		dm.level1_context_name , dm.level2_event_name , dm.level3_action_name
	)
SELECT medical_record_number , encounter_key , facility_key , age_in_days_key, time_of_day_key,
	procedure_key  , Clinical_Result_Numeric, Clinical_Result_String, Reference_Range,  Abnormal_Flag, unit_of_measure_numeric,
	case when unit_of_measure_string in ('SHORT TEXT', 'SHORT DESCRIPTION') then NULL else unit_of_measure_string end as unit_of_measure_string,
	case when unit_of_measure_reference_range ~ 'SHORT TEXT' then NULL else unit_of_measure_reference_range end as unit_of_measure_reference_range
FROM lab_flat;