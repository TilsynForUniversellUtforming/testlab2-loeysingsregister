create schema Loeysingsregister;

set search_path to Loeysingsregister;

create table Verksemd
(
    id                     serial primary key,
    orgnummer              varchar(9)  not null,
    namn                   varchar(50) not null,
    orgkode                varchar     not null,
    naeringskode           varchar     not null,
    naringskodeBeskrivelse varchar     not null,
    sektorkode             varchar     not null,
    sektorkodeBeskrivelse  varchar     not null,
    orgformkode            varchar     not null,
    orgformBeskrivelse     varchar     not null,
    talTilsette            varchar     not null,
    postnr                 varchar(4)  not null,
    poststad               varchar     not null
);

create table Dokument
(
    id          serial primary key,
    url         varchar not null,
    typeTeneste varchar not null,
    prosess     varchar not null,
    verksemdId  int references Verksemd (id)
);

create table Automat
(
    id                   serial primary key,
    namn                 varchar not null,
    geografiskPlassering varchar not null,
    verksemdId           int references Verksemd (id)
);

create table App
(
    id                       serial primary key,
    namn                     varchar not null,
    operativsystem           varchar not null,
    antallNedlastinger       int     not null,
    versjon                  varchar not null,
    adresse                  varchar not null,
    tenesteomraade           varchar not null,
    prioriterAvInteressentar boolean not null,
    verksemdId               int references Verksemd (id)
);

create table Appside
(
    id            serial primary key,
    beskrivelse   varchar not null,
    appsideNummer int     not null,
    appId         int references Appside (id)
);

create table Nettstad
(
    id                       serial primary key,
    namn                     varchar not null,
    nettadresse              varchar not null,
    typeNettstad             varchar not null,
    tenesteomraade           varchar not null,
    typeTenester             varchar not null,
    prioriterAvInteressentar boolean not null,
    verksemdId               int references Verksemd (id)
);

create table Nettside
(
    url         varchar primary key,
    sideType    varchar not null,
    prosess     varchar not null,
    typeTeneste varchar not null,
    nettstadId  int references Nettstad (id)
)