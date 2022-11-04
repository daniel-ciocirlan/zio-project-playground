CREATE TABLE IF NOT EXISTS "reviews"
(
    "id"         BIGSERIAL PRIMARY KEY,
    "company_id" BIGINT      NOT NULL,
    "user_id"    BIGINT      NOT NULL,
    "txt"        TEXT        NOT NULL,
    "date"       timestamptz NOT NULL,
    CONSTRAINT "fk_company" FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT "fk_user" FOREIGN KEY (user_id) REFERENCES users (id)
);
