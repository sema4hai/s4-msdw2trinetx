-- there might be cases where one procedure_key is mapped to multiple entries
-- it was a serious issue with earlier versions of DMSDW; not a big issue for MSDW
CREATE TABLE hai_az_prod.fd_procedure AS
SELECT procedure_key, listagg(context_name, ' | ') within group (order by context_name , context_procedure_code ) as context_name ,
listagg(context_procedure_code , ' | ') within group (order by context_name , context_procedure_code ) as context_procedure_code ,
listagg(DISTINCT procedure_description, ' | ') within group (order by context_name , context_procedure_code ) as procedure_description
FROM prod_msdw.fd_procedure fp
GROUP BY procedure_key;