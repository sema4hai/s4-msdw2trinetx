CREATE TABLE pd_prod_db.encounter_dmsdw_2020q4 AS
SELECT  de.medical_record_number,
de.encounter_key, de.encounter_visit_id, de.encounter_sub_visit_id ,
de.msdw_encounter_type as encounter_type,
de.encounter_service as location,
begin_date_time_age_in_days, end_date_time_age_in_days, estimated_length_of_stay
FROM dmsdw_2020q4.d_encounter de;