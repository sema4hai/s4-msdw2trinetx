-- medication
-- medication for EPIC Administered
create or replace view msdw_med_epic_admin AS
SELECT dp.medical_record_number ,
    f.person_key,
    f.encounter_key,
    de.encounter_visit_id,
    f.facility_key,
    f.caregiver_group_key,
    f.operation_key,
    dc.calendar_date ,
    f.time_of_day_key ,
	  f.material_group_key,
	  bm.material_rank,
	  bm.material_role ,
	  fdm.material_type ,
	  fdm.material_name ,
	  fdm.context_name ,
	  fdm.context_material_code ,
	  dm.level1_context_name ,
	  dm.level2_event_name ,
	  dm.level3_action_name,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Administered Status' then f.value end, '|') as Administered_Status,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Administered Unit' then f.value end, '|') as Administered_Unit,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Comments' then f.value end, '|') as Comments,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Due Date' then f.value end, '|') as Due_Date,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Encounter ID' then f.value end, '|') as Encounter_ID,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Entered By' then f.value end, '|') as Entered_By,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Facility ID' then f.value end, '|') as Facility_ID,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Infusion Rate' then f.value end, '|') as Infusion_Rate,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Medication ID' then f.value end, '|') as Medication_ID,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Organization ID' then f.value end, '|') as Organization_ID,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Person ID' then f.value end, '|') as Person_ID,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Reason' then f.value end, '|') as Reason,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Entry Date' then f.value end, '|') as Entry_Date,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Route' then f.value end, '|') as Route,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Site' then f.value end, '|') as Site,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Administered Date' then f.value end, '|') as Administered_Date
	FROM hai_az_test.fact f
	JOIN prod_msdw.d_person dp on f.person_key = dp.person_key
	JOIN prod_msdw.d_encounter de on f.encounter_key = de.encounter_key
	JOIN prod_msdw.b_material bm on f.material_group_key = bm.material_group_key
	JOIN prod_msdw.fd_material fdm on bm.material_key = fdm.material_key
	JOIN prod_msdw.d_metadata dm on f.meta_data_key = dm.meta_data_key
	JOIN prod_msdw.d_unit_of_measure duom on f.uom_key = duom.uom_key
	join prod_msdw.d_calendar dc using (calendar_key)
	where bm.material_role = 'Medication'
	and dm.level1_context_name = 'EPIC'
	and dm.level2_event_name = 'Medication Administration'
	AND dm.level3_action_name = 'Given'
	GROUP BY dp.medical_record_number,
	     f.person_key,
	     f.encounter_key,
	     de.encounter_visit_id,
	     f.facility_key,
	     f.caregiver_group_key,
	     f.operation_key,
	     dc.calendar_date ,
	     f.time_of_day_key ,
	     f.material_group_key,
	     bm.material_rank,
	     bm.material_role ,
	     fdm.material_type ,
	     fdm.material_name ,
	     fdm.context_name ,
	     fdm.context_material_code ,
	     dm.level1_context_name ,
	    dm.level2_event_name ,
	    dm.level3_action_name;

-- medication for EPIC Prescription
create or replace view msdw_med_epic_prscb AS
SELECT dp.medical_record_number,
    f.person_key,
    f.encounter_key,
    de.encounter_visit_id,
    f.facility_key,
    f.caregiver_group_key,
    f.operation_key,
    dc.calendar_date ,
    f.time_of_day_key ,
	  f.material_group_key,
	  bm.material_rank,
	  bm.material_role ,
	  fdm.material_type ,
	  fdm.material_name ,
	  fdm.context_name ,
	  fdm.context_material_code ,
	  dm.level1_context_name ,
	  dm.level2_event_name ,
	  dm.level3_action_name,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Facility ID' then f.value end, '|') as Facility_ID,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Organization ID' then f.value end, '|') as Organization_ID,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Encounter ID' then f.value end, '|') as Encounter_ID,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Medication ID' then f.value end, '|') as Medication_ID,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'SIG' then f.value end, '|') as SIG,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Route' then f.value end, '|') as Route,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Refills' then f.value end, '|') as Refills,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Dispense' then f.value end, '|') as Dispense,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Order Class' then f.value end, '|') as Order_Class,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Person ID' then f.value end, '|') as Person_ID,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'End Date' then f.value end, '|') as End_Date,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Entry Date' then f.value end, '|') as Entry_Date,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Ordering Provider ID' then f.value end, '|') as Ordering_Provider_ID,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Entered By' then f.value end, '|') as Entered_By,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Start Date' then f.value end, '|') as Start_Date
	FROM hai_az_test.fact f
	JOIN prod_msdw.d_person dp on f.person_key = dp.person_key
	JOIN prod_msdw.d_encounter de on f.encounter_key = de.encounter_key
	JOIN prod_msdw.b_material bm on f.material_group_key = bm.material_group_key
	JOIN prod_msdw.fd_material fdm on bm.material_key = fdm.material_key
	JOIN prod_msdw.d_metadata dm on f.meta_data_key = dm.meta_data_key
	join prod_msdw.d_calendar dc using (calendar_key)
	where bm.material_role = 'Medication'
	and dm.level1_context_name = 'EPIC'
	and dm.level2_event_name = 'Prescription'
	and dm.level3_action_name = 'Sent'
	GROUP BY dp.medical_record_number ,
	     f.person_key,
	     f.encounter_key,
	     de.encounter_visit_id,
	     f.facility_key,
	     f.caregiver_group_key,
	     f.operation_key,
	     dc.calendar_date ,
	     f.time_of_day_key ,
	     f.material_group_key,
	     bm.material_rank,
	     bm.material_role ,
	     fdm.material_type ,
	     fdm.material_name ,
	     fdm.context_name ,
	     fdm.context_material_code ,
	     dm.level1_context_name ,
	     dm.level2_event_name ,
	     dm.level3_action_name ;

--medication for TDS Administered
create or replace view msdw_med_tds_admin AS
SELECT dp.medical_record_number,
    f.person_key,
    f.encounter_key,
    de.encounter_visit_id,
    f.facility_key,
    f.caregiver_group_key,
    f.operation_key,
    dc.calendar_date ,
    f.time_of_day_key ,
	  f.material_group_key,
	  bm.material_rank,
	  bm.material_role ,
	  fdm.material_type ,
	  fdm.material_name ,
	  fdm.context_name ,
	  fdm.context_material_code ,
	  dm.level1_context_name ,
	  dm.level2_event_name ,
	  dm.level3_action_name,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Administered Unit' and f.value != '<null>' then f.value end, '|') as Administered_Unit,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Medication Route' then f.value end, '|') as Medication_Route,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Administered Note' then f.value end, '|') as Administered_Note,
	LISTAGG(DISTINCT case when dm.level4_field_name = 'Route Detail' then f.value end, '|') as Route_Detail
	FROM hai_az_test.fact f
	JOIN prod_msdw.d_person dp on f.person_key = dp.person_key
	JOIN prod_msdw.d_encounter de on f.encounter_key = de.encounter_key
	JOIN prod_msdw.b_material bm on f.material_group_key = bm.material_group_key
	JOIN prod_msdw.fd_material fdm on bm.material_key = fdm.material_key
	JOIN prod_msdw.d_metadata dm on f.meta_data_key = dm.meta_data_key
	join prod_msdw.d_calendar dc using (calendar_key)
	where bm.material_role = 'Medication'
	and dm.level1_context_name = 'TDS'
	and dm.level2_event_name = 'Medication Administration'
	and level3_action_name = 'Given'
	GROUP BY dp.medical_record_number,
	     f.person_key, f.encounter_key, de.encounter_visit_id, f.facility_key, f.caregiver_group_key, f.operation_key, dc.calendar_date , f.time_of_day_key ,
	f.material_group_key, bm.material_rank, bm.material_role , fdm.material_type , fdm.material_name , fdm.context_name , fdm.context_material_code ,
	dm.level1_context_name , dm.level2_event_name , dm.level3_action_name ;


-- Create final table
create table hai_az_test.medications_2020July as
With med_1 as (SELECT distinct medical_record_number,
    person_key,
    encounter_key,
    encounter_visit_id,
    facility_key,
    caregiver_group_key,
    operation_key,
    calendar_date,
    time_of_day_key ,
	  material_group_key,
	  material_rank,
	  material_role ,
	  material_type ,
	  material_name ,
	  context_name ,
	  context_material_code ,
	  level1_context_name ,
	  level2_event_name ,
	  level3_action_name,
	  Administered_Unit as units_per_administration ,
    Route,
	  null as Route_Detail,
    Site,
    Infusion_Rate,
	  null as Refills,
	  null as Dispense,
	  null as Start_Date,
	  null as End_Date,
	  null as	Note ,
	  'Administration' as source
FROM msdw_med_epic_admin),
med_2 as (
SELECT distinct medical_record_number,
    person_key,
    encounter_key,
    encounter_visit_id,
    facility_key,
    caregiver_group_key,
    operation_key,
    calendar_date,
    time_of_day_key ,
	  material_group_key,
	  material_rank,
	  material_role ,
	  material_type ,
	  material_name ,
	  context_name ,
	  context_material_code ,
	  level1_context_name ,
	  level2_event_name ,
	  level3_action_name,
	  null as units_per_administration ,
	  Route,
	  null as Route_Detail,
	  null as Site,
	  null as Infusion_Rate,
	  Refills,
	  Dispense,
	  Start_Date,
	  End_Date,
	  SIG as Note ,
	  'Prescription' as source
	FROM msdw_med_epic_prscb),
med_3 as (
SELECT distinct medical_record_number,
    person_key,
    encounter_key,
    encounter_visit_id,
    facility_key,
    caregiver_group_key,
    operation_key,
    calendar_date,
    time_of_day_key ,
	  material_group_key,
	  material_rank,
	  material_role ,
	  material_type ,
	  material_name ,
	  context_name ,
	  context_material_code ,
	  level1_context_name ,
	  level2_event_name ,
	  level3_action_name,
	  Administered_Unit as units_per_administration ,
	  Medication_Route AS Route,
	  Route_Detail,
	  null as Site,
	  null as Infusion_Rate,
	  null as Refills,
	  null as Dispense,
	  null as Start_Date,
	  null as End_Date,
	  Administered_Note as Note ,
	  'Administration' as source
	  FROM msdw_med_tds_admin)
SELECT distinct *
FROM med_1
UNION ALL
select distinct *
FROM med_2
UNION ALL
select distinct *
FROM med_3;