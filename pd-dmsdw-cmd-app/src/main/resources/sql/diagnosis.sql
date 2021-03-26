-- Below is a more efficient way to pivot diagnoses in fact table
-- I have confirmed that the pivot keys are sufficient: operation_key and data_feed_key seem not necessary
DROP VIEW IF EXISTS epic_primary_wide;
create or replace view epic_primary_wide as
SELECT person_key , encounter_key, caregiver_group_key, facility_key, diagnosis_group_key, age_in_days_key ,
 LISTAGG(DISTINCT case when meta_data_key = 5719 then value end, '|') as Dx_Annotation,
 LISTAGG(DISTINCT case when meta_data_key = 5720 then value end, '|') as Dx_qualifier,
 LISTAGG(DISTINCT case when meta_data_key = 5721 then value end, '|') as Primary_Dx_FLAG,
 LISTAGG(DISTINCT case when meta_data_key = 5722 then value end, '|') as Chronic_Dx_Flag,
 LISTAGG(DISTINCT case when meta_data_key = 5723 then value end, '|') as Dx_Comments
FROM pd_test_db.regain_fact fact
left join dmsdw_2020q2.b_diagnosis bd using (diagnosis_group_key)
where bd.diagnosis_role = 'Primary'
GROUP BY person_key , encounter_key, caregiver_group_key, facility_key, diagnosis_group_key, age_in_days_key;

-- Join to other dict to get required information. Alternatively, we can just use diagnosis_group_key to save space.
-- still need to add the meta data information: level 1-3 for meta_data_key 5719-5723
DROP VIEW IF EXISTS epic_primary;
CREATE OR REPLACE VIEW epic_primary AS
SELECT p.medical_record_number , f.encounter_key, f.caregiver_group_key, f.diagnosis_group_key ,bd.diagnosis_rank , bd.diagnosis_key , fdd.context_name , fdd.context_diagnosis_code , fdd.description ,
f.age_in_days_key,
case when f.Primary_Dx_FLAG ~ 'Yes' then 'Primary' else 'Secondary' end as principal_diagnosis_indicator,  'EPIC Primary' AS diagnosis_source
FROM epic_primary_wide f
left join dmsdw_2020q2.d_person p using (person_key)
left join dmsdw_2020q2.b_diagnosis bd using (diagnosis_group_key)
left join dmsdw_2020q2.fd_diagnosis fdd using (diagnosis_key);

-- check whether there are multiple values
SELECT EXISTS (SELECT * FROM epic_primary_wide where primary_dx_flag ~ '.*\\|.*');

-- diagnoses with a diagnosis_role of "principal"
DROP VIEW IF EXISTS diag_principle_staging;
CREATE OR REPLACE VIEW diag_principle_staging AS
SELECT distinct
dp.medical_record_number , f.encounter_key , f.caregiver_group_key, f.facility_key, f.value,
bd.diagnosis_role, bd.diagnosis_group_key , bd.diagnosis_rank,  bd.diagnosis_key ,
fdd.context_name, fdd.context_diagnosis_code, fdd.description,
f.age_in_days_key, f.time_of_day_key , f.meta_data_key, dm.level1_context_name, dm.level2_event_name, dm.level3_action_name, dm.level4_field_name
FROM pd_test_db.regain_fact f
JOIN dmsdw_2020q2.d_person dp on f.person_key = dp.person_key
JOIN dmsdw_2020q2.b_diagnosis bd ON f.diagnosis_group_key = bd.diagnosis_group_key
JOIN dmsdw_2020q2.fd_diagnosis fdd on bd.diagnosis_key = fdd.diagnosis_key
JOIN dmsdw_2020q2.d_metadata dm on f.meta_data_key = dm.meta_data_key
where bd.diagnosis_role = 'Principal';

DROP VIEW IF EXISTS diag_principle;
CREATE OR REPLACE VIEW diag_principle AS
SELECT medical_record_number, encounter_key, caregiver_group_key, diagnosis_group_key, diagnosis_rank, diagnosis_key, context_name, context_diagnosis_code, description,
       age_in_days_key, true as principle_diagnosis_indicator, 'Principal' as diagnosis_source
FROM diag_principle_staging;


-- problem list
drop view if EXISTS problem_list_staging;
CREATE OR REPLACE VIEW problem_list_staging AS
SELECT distinct
dp.medical_record_number , f.encounter_key , f.caregiver_group_key, f.facility_key, f.value, f.uom_key ,
bd.diagnosis_role, bd.diagnosis_group_key , bd.diagnosis_rank,  bd.diagnosis_key ,
fdd.context_name, fdd.context_diagnosis_code, fdd.description,
f.age_in_days_key, f.time_of_day_key , f.meta_data_key, dm.level1_context_name, dm.level2_event_name, dm.level3_action_name, dm.level4_field_name
FROM pd_test_db.regain_fact f
JOIN dmsdw_2020q2.d_person dp on f.person_key = dp.person_key
JOIN dmsdw_2020q2.b_diagnosis bd ON f.diagnosis_group_key = bd.diagnosis_group_key
JOIN dmsdw_2020q2.fd_diagnosis fdd on bd.diagnosis_key = fdd.diagnosis_key
JOIN dmsdw_2020q2.d_metadata dm on f.meta_data_key = dm.meta_data_key
where bd.diagnosis_role = 'Problem List' and f.meta_data_key in (3490, 3491);

DROP VIEW IF EXISTS problem_list;
CREATE OR REPLACE VIEW problem_list AS
SELECT distinct medical_record_number, encounter_key, caregiver_group_key, diagnosis_group_key, diagnosis_rank, diagnosis_key, context_name, context_diagnosis_code, description,
       age_in_days_key, 'Unknown' as principle_diagnosis_indicator, 'Problem List' as diagnosis_source
 FROM problem_list_staging;


-- reason for visit
drop view if exists reason_visit_wide;
create or replace view reason_visit_wide AS
SELECT person_key, encounter_key, caregiver_group_key, facility_key, diagnosis_group_key, age_in_days_key, time_of_day_key,
LISTAGG(DISTINCT case when meta_data_key = 5719 then value end, '|') as Dx_Annotation,
 LISTAGG(DISTINCT case when meta_data_key = 5720 then value end, '|') as Dx_qualifier,
 LISTAGG(DISTINCT case when meta_data_key = 5721 then value end, '|') as Primary_Dx_FLAG,
 LISTAGG(DISTINCT case when meta_data_key = 5722 then value end, '|') as Chronic_Dx_Flag,
 LISTAGG(DISTINCT case when meta_data_key = 5723 then value end, '|') as Dx_Comments
FROM pd_test_db.regain_fact fact
JOIN dmsdw_2020q2.b_diagnosis bd using (diagnosis_group_key)
where bd.diagnosis_role = 'Reason For Visit'
GROUP BY person_key, encounter_key, caregiver_group_key, facility_key, diagnosis_group_key, age_in_days_key, time_of_day_key;

DROP VIEW IF EXISTS reason_visit;
CREATE OR REPLACE VIEW reason_visit AS
SELECT distinct p.medical_record_number , f.encounter_key, f.caregiver_group_key, f.diagnosis_group_key, bd.diagnosis_rank , bd.diagnosis_key ,
fdd.context_name , fdd.context_diagnosis_code , fdd.description ,
f.age_in_days_key ,
case when f.Primary_Dx_FLAG ~ 'Yes' then 'Primary' else 'Secondary' end as principal_diagnosis_indicator,
'Reason For Visit' as diagnosis_source
FROM reason_visit_wide f
left join dmsdw_2020q2.d_person p using (person_key)
left join dmsdw_2020q2.b_diagnosis bd using (diagnosis_group_key)
left join dmsdw_2020q2.fd_diagnosis fdd using (diagnosis_key);



-- secondary diagnosis
DROP VIEW IF EXISTS secondary_diag_staging;
CREATE OR REPLACE VIEW secondary_diag_staging AS
SELECT distinct
dp.medical_record_number , f.encounter_key , f.caregiver_group_key, f.facility_key, f.value, f.uom_key ,
bd.diagnosis_role, bd.diagnosis_group_key , bd.diagnosis_rank,  bd.diagnosis_key ,
fdd.context_name, fdd.context_diagnosis_code, fdd.description,
f.age_in_days_key, f.meta_data_key, dm.level1_context_name, dm.level2_event_name, dm.level3_action_name
FROM pd_test_db.regain_fact f
JOIN dmsdw_2020q2.d_person dp on f.person_key = dp.person_key
JOIN dmsdw_2020q2.b_diagnosis bd ON f.diagnosis_group_key = bd.diagnosis_group_key
JOIN dmsdw_2020q2.fd_diagnosis fdd on bd.diagnosis_key = fdd.diagnosis_key
JOIN dmsdw_2020q2.d_metadata dm on f.meta_data_key = dm.meta_data_key
where bd.diagnosis_role = 'Secondary';

DROP VIEW IF EXISTS secondary_diag;
CREATE OR REPLACE VIEW secondary_diag AS
SELECT medical_record_number, encounter_key, caregiver_group_key, diagnosis_group_key, diagnosis_rank, diagnosis_key, context_name, context_diagnosis_code, description, age_in_days_key,
'Secondary' as principal_diagnosis_indicator, 'Secondary' as diagnosis_source
FROM secondary_diag_staging;


CREATE TABLE pd_test_db diagnosis_2020q2 AS
SELECT * FROM epic_primary
UNION ALL
SELECT * FROM diag_principle
UNION ALL
SELECT * FROM problem_list
UNION ALL
SELECT * FROM reason_visit
UNION ALL
SELECT * FROM secondary_diag;