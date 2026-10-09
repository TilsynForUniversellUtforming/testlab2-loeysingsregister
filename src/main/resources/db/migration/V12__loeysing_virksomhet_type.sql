ALTER TABLE loeysing
ADD COLUMN loeysingstype TEXT NOT NULL DEFAULT 'nett';

ALTER TABLE verksemd
ADD COLUMN virksomhetstype TEXT;