begin;
alter table loeysing
    add column aktiv boolean;
alter table loeysing
    add column original int;
alter table loeysing
    add column tidspunkt timestamptz;
update loeysing
set aktiv     = true,
    original  = id,
    tidspunkt = now();
commit;