CREATE TABLE hai_az_test.encounter_dmsdw_2020July as
SELECT de.medical_record_number,
de.encounter_key, de.encounter_visit_id, de.encounter_sub_visit_id ,
de.msdw_encounter_type as encounter_type,
de.encounter_service as location,
begin_date_time, end_date_time, estimated_length_of_stay
FROM prod_msdw.d_encounter de;