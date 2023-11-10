update loeysing
set tidspunkt = '2023-01-01T00:00:00'::timestamp with time zone at time zone 'Europe/Oslo'
where id = original;