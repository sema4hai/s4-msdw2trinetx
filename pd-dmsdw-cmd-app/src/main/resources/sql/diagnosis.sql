-- Below is a more efficient way to pivot diagnoses in fact table
-- I have confirmed that the pivot keys are sufficient: operation_key and data_feed_key seem not necessary
create or replace view epic_primary_wide as
SELECT person_key , encounter_key, caregiver_group_key, facility_key, diagnosis_group_key, age_in_days_key , time_of_day_key,
 LISTAGG(DISTINCT case when meta_data_key = 5719 then value end, '|') as Dx_Annotation,
 LISTAGG(DISTINCT case when meta_data_key = 5720 then value end, '|') as Dx_qualifier,
 LISTAGG(DISTINCT case when meta_data_key = 5721 then value end, '|') as Primary_Dx_FLAG,
 LISTAGG(DISTINCT case when meta_data_key = 5722 then value end, '|') as Chronic_Dx_Flag,
 LISTAGG(DISTINCT case when meta_data_key = 5723 then value end, '|') as Dx_Comments
FROM pd_test_db.regain_fact fact
GROUP BY person_key , encounter_key, caregiver_group_key, facility_key, diagnosis_group_key, age_in_days_key, time_of_day_key;

-- Join to other dict to get required information. Alternatively, we can just use diagnosis_group_key to save space.
-- still need to add the meta data information: level 1-3 for meta_data_key 5719-5723
SELECT p.medical_record_number , f.encounter_key, f.caregiver_group_key, f.facility_key, f.diagnosis_group_key, f.age_in_days_key , f.time_of_day_key, f.Primary_Dx_FLAG as principal_diagnosis_indicator,
bd.diagnosis_rank , bd.diagnosis_key , fdd.context_name , fdd.context_diagnosis_code , fdd.description
FROM epic_primary_wide f
left join dmsdw_2020q2.d_person p using (person_key)
left join dmsdw_2020q2.b_diagnosis bd using (diagnosis_group_key)
left join dmsdw_2020q2.fd_diagnosis fdd using (diagnosis_key)
where bd.diagnosis_role = 'Primary';