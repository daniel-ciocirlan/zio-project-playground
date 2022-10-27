CREATE TABLE IF NOT EXISTS "companies"
(
    "id"      BIGSERIAL PRIMARY KEY,
    "slug"    TEXT UNIQUE NOT NULL,
    "name"    TEXT NOT NULL,
    "hq"      JSON,
    "offices" JSON
);
