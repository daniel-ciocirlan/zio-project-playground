CREATE TABLE IF NOT EXISTS "users"
(
    "id"        BIGSERIAL PRIMARY KEY,
    "user_name" TEXT NOT NULL,
    "pw_hash"   TEXT NOT NULL
);
CREATE INDEX "user_name_idx" ON "users" ("user_name");
