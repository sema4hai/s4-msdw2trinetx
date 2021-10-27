drop table if exists hai_az_test.lab_epic_2020July;
CREATE TABLE hai_az_test.lab_epic_2020July as
-- use a subset for testing
with epic_lab as (
    select el.*
    from prod_msdw.epic_lab el
    join ct_fabry."_cohort_by_dx" cbd using (mrn)
),
lab_epic_staging as (
	SELECT mrn, order_date , lab_time , test_code , test_result_value ,
	case when trim(unit_of_measurement) in ('SHORT TEXT', 'SHORT DESCRIPTION', '') then NULL else trim(unit_of_measurement) end as unit_of_measurement,
	reference_range,
	REGEXP_SUBSTR(test_result_value , '\\-?[0-9]+((,[0-9]+)*)(\\.[0-9]+((,[0-9]+)?))?') as test_result_value_num,
	SPLIT_PART(REPLACE(reference_range , ' ', ''), '-', 1)  as range_l,
	SPLIT_PART(REPLACE(reference_range , ' ', ''), '-', 2)  as range_r,
	REGEXP_SUBSTR(range_l , '\\-?[0-9]+((,[0-9]+)*)(\\.[0-9]+((,[0-9]+)?))?') as range_low,
	REGEXP_SUBSTR(range_r , '\\-?[0-9]+((,[0-9]+)*)(\\.[0-9]+((,[0-9]+)?))?') as range_hi,
	REPLACE(test_result_value_num, ',', '') :: REAL as test_result_value_final,
	REPLACE(range_low, ',', '') :: REAL as range_low_final,
	REPLACE(range_hi, ',', '') :: REAL  as range_hi_final
	FROM epic_lab el
	where test_result_value ~ '^[0-9]+(\\.[0-9]+((,[0-9]+)?))?'
	and reference_range ~ '^[0-9]+(\\.[0-9]+((,[0-9]+)?))?\\s?-\\s?[0-9]+(\\.[0-9]+((,[0-9]+)?))?'
	-- the following line filters out a few records with ridiculouly large value
	and length(REGEXP_SUBSTR(test_result_value , '\\-?[0-9,]+') ) < 30
--	limit 1000
)
select mrn, order_date , lab_time , test_code , test_result_value , unit_of_measurement , reference_range , test_result_value_final, range_low_final, range_hi_final
FROM lab_epic_staging ;