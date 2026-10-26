CREATE TABLE users (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255)  NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ   NOT NULL,
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

CREATE TABLE balances (
    id          UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID             NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    asset       VARCHAR(10)      NOT NULL,
    available   NUMERIC(38,18)   NOT NULL DEFAULT 0,
    locked      NUMERIC(38,18)   NOT NULL DEFAULT 0,
    version     BIGINT           NOT NULL DEFAULT 0,
    updated_at  TIMESTAMPTZ      NOT NULL DEFAULT now(),
    UNIQUE (user_id, asset),
    CHECK (available >= 0),
    CHECK (locked >= 0)
);