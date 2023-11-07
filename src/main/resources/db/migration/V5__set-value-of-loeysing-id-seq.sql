begin;
select setval('loeysing_id_seq', (select max(id) from loeysing));
commit;