# testlab2-loeysingsregister

Modul for registser av ikt-løysingar og virksomheter for kontroll
Støtte for utvalprosess.

## Lagring av løysingar med historikk

Løysingar lagres i databasen med historikk. Det vil seie at det er mulig å se på ei løysing slik den var på eit gitt
tidspunkt.

Løysingane er lagra i tabellen 'loeysing'. Første gang ei løysing blir lagra, blir alle felta lagra i tabellen.
Oppdateringar blir lagra som ei ny rad i tabellen, men bare dei felta som er endra blir lagra. Radane som høyrer saman
har same verdi i feltet 'original', som er id-en til den første raden i serien.

Når ei løysing lesast frå databasen, blir alle radane som høyrer til den løysinga lest ut, og satt saman til ei komplett
løysing.

## Utvikling

### Formatering

All kotlinkode skal formateres med ktfmt og default stil. Det blir sjekket automatisk ved alle bygg med
[spotless-maven-plugin](https://github.com/diffplug/spotless/tree/main/plugin-maven).

#### Formatering i IDEA

Installer plugin-en [ktfmt](https://plugins.jetbrains.com/plugin/14912-ktfmt) for å formatere med ktfmt i IDEA. Husk å
skru den på i settings etter at du har installert den.

Du kan også formatere automatisk når du lagrer ved å skru på `Settings -> Tools -> Actions on Save -> Reformat code`.

### Statisk kodeanalyse

Vi bruker SonarLint i IDEA til kodeanalyse. Installer
plugin-en [SonarLint](https://plugins.jetbrains.com/plugin/7973-sonarlint). Den krever også at du har installert NodeJS. 