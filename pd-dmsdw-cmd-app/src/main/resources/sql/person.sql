-- patient
-- date of birth
CREATE OR REPLACE VIEW person_dob AS
WITH dob_rank as (
	SELECT medical_record_number , date_of_birth, ROW_NUMBER() over (PARTITION by medical_record_number order by count(*) desc ) as date_of_birth_rank
	FROM dmsdw_2020q4.d_person dp
	group by medical_record_number, date_of_birth)
SELECT medical_record_number, date_of_birth
FROM dob_rank
where date_of_birth_rank = 1;

-- month_of_birth
CREATE OR REPLACE VIEW person_mob AS
WITH mob_rank as (
	SELECT medical_record_number , month_of_birth, ROW_NUMBER() over (PARTITION by medical_record_number order by count(*) desc ) as month_of_birth_rank
	FROM dmsdw_2020q4.d_person dp
	group by medical_record_number, month_of_birth
)
SELECT medical_record_number , month_of_birth
FROM mob_rank
WHERE month_of_birth_rank = 1;

-- gender
CREATE OR REPLACE VIEW person_gender AS
with gender_rank as (
	SELECT medical_record_number , gender , ROW_NUMBER() over (PARTITION by medical_record_number order by count(*) desc ) as gender_rank
	FROM dmsdw_2020q4.d_person dp
	group by medical_record_number, gender
)
SELECT medical_record_number , gender
FROM gender_rank
WHERE gender_rank = 1;

-- race
CREATE OR REPLACE VIEW person_race AS
with normalized_race as (
	SELECT distinct medical_record_number , mapto
	FROM dmsdw_2020q4.d_person
	left join pd_prod_db.dmsdw_race_map rm on trim(d_person.race) = rm.race or (trim(d_person.race) = '' and rm.race is null) or (d_person.race is null and rm.race is null)
	group by medical_record_number, mapto
),
mixed_race as (
	SELECT medical_record_number, 'mixed' as race
	from normalized_race
	where mapto != 'Unknown'
	group by medical_record_number
	having count(mapto) > 1),
unique_race as (
	SELECT medical_record_number, mapto as race
	from normalized_race
	where mapto != 'Unknown' and medical_record_number in (
			SELECT medical_record_number
			from normalized_race
			where mapto != 'Unknown'
			group by medical_record_number
			having count(mapto ) = 1)
),
known_race as (
	SELECT *
	FROM mixed_race
	union all
	SELECT *
	FROM unique_race
),
all_patient_mrn as (
	SELECT distinct medical_record_number
	FROM d_person
),
combine as (
	SELECT *
	FROM all_patient_mrn
	left join known_race using (medical_record_number)
)
select medical_record_number, case when race is null then 'Unknown' else race end as race
FROM combine
order by medical_record_number;

-- patient demographics
drop table if exists pd_prod_db.person_dmsdw_2020q4;
create table pd_prod_db.person_dmsdw_2020q4 AS
with all_patient_mrn as (
	SELECT distinct medical_record_number
	FROM dmsdw_2020q4.d_person
)
SELECT *
FROM all_patient_mrn
left join person_dob using (medical_record_number)
left join person_mob using (medical_record_number)
left join person_gender using (medical_record_number)
left join person_race using (medical_record_number);