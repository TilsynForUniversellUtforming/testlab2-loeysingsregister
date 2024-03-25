create table verksemd
(
    id serial primary key,
    namn text not null,
    orgnummer text not null,
    institusjonell_sektorkode text not null,
    institusjonell_sektorkode_beskrivelse text not null,
    naeringskode text not null,
    naeringskode_beskrivelse text not null,
    organisasjonsform_kode text not null,
    organsisasjonsform_omtale text not null,
    fylkesnummer varchar(4) not null,
    fylke text not null,
    kommunenummer varchar(4) not null,
    kommune text not null,
    postnummer varchar(4),
    poststad text,
    tal_tilsette int not null,
    forvaltningsnivaa text,
    tenesteromraade text,
    under_avviking boolean not null default false,
    aktiv boolean not null default true,
    original int,
    tidspunkt timestamptz default now()






);