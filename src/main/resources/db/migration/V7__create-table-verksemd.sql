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
    fylkesnummer int not null,
    fylke text not null,
    kommunenummer int not null,
    kommune text not null,
    postnummer int not null,
    poststad text not null,
    tal_tilsette int not null,
    forvaltningsnivaa text not null,
    tenesteromraade text not null,
    aktiv boolean not null default true,
    original int,
    tidspunkt timestamptz default now()






);