CREATE TABLE IF NOT EXISTS "users"
(
    "id"        BIGSERIAL PRIMARY KEY,
    "user_name" TEXT NOT NULL,
    "pw_hash"   TEXT NOT NULL
);
CREATE INDEX "user_name_idx" ON "users" ("user_name");

CREATE TABLE IF NOT EXISTS "companies"
(
    "id"   BIGSERIAL PRIMARY KEY,
    "slug" TEXT UNIQUE NOT NULL,
    "name" TEXT        NOT NULL,
    "url"  TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS "reviews"
(
    "id"            BIGSERIAL PRIMARY KEY,
    "company_id"    BIGINT    NOT NULL,
    "submitted_by"  BIGINT    NOT NULL,
    management      INT       NOT NULL,
    culture         INT       NOT NULL,
    salary          INT       NOT NULL,
    benefits        INT       NOT NULL,
    would_recommend INT       NOT NULL,
    review          TEXT      NOT NULL,
    created         TIMESTAMP NOT NULL DEFAULT now(),
    updated         TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (submitted_by) REFERENCES users (id) ON DELETE CASCADE
);
