CREATE TABLE hai_az_prod.loinc_testable_phenotypes AS
WITH directly_tested_abnormality AS (
    SELECT *
    FROM hai_az_prod.loinc2hpo lh2
    where not isnegated ),
    directly_tested_and_inferred_abnormality as (
    SELECT *
    FROM directly_tested_abnormality dta
    JOIN hai_az_prod.hpo_is_a_pairs hiap on dta.hpotermid = hiap."current"
),
add_directly_tested_flag as (
    SELECT dtia.loincid, dtia.ancestor as termid, (dtia."current" = dtia.ancestor) as is_tested_directly
    FROM directly_tested_and_inferred_abnormality dtia
)
SELECT distinct loincid , termid, is_tested_directly
FROM add_directly_tested_flag
order by loincid, termid ;