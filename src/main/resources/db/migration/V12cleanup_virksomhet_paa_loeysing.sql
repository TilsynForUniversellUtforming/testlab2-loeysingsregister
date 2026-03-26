UPDATE loeysing l1
SET verksemd_id = (
    SELECT distinct id
    FROM verksemd
    WHERE organisasjonsnummer = l1.orgnummer
)
WHERE l1.verksemd_id IS NULL;