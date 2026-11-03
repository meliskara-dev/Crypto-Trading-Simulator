CREATE TABLE orders (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL REFERENCES users(id),
    symbol          VARCHAR(20)     NOT NULL,
    side            VARCHAR(4)      NOT NULL,
    type            VARCHAR(10)     NOT NULL,
    price           NUMERIC(38, 18),
    quantity        NUMERIC(38, 18) NOT NULL,
    filled_quantity NUMERIC(38, 18) NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL,
    idempotency_key VARCHAR(64)     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    version         BIGINT          NOT NULL DEFAULT 0,

    CONSTRAINT uk_orders_user_idempotency UNIQUE (user_id, idempotency_key),
    CONSTRAINT ck_orders_side     CHECK (side IN ('BUY', 'SELL')),
    CONSTRAINT ck_orders_type     CHECK (type IN ('LIMIT', 'MARKET')),
    CONSTRAINT ck_orders_status   CHECK (status IN ('NEW', 'PARTIALLY_FILLED', 'FILLED', 'CANCELLED', 'REJECTED')),
    CONSTRAINT ck_orders_price    CHECK ((type = 'LIMIT' AND price IS NOT NULL AND price > 0) OR (type = 'MARKET' AND price IS NULL)),
    CONSTRAINT ck_orders_quantity CHECK (quantity > 0),
    CONSTRAINT ck_orders_filled   CHECK (filled_quantity >= 0 AND filled_quantity <= quantity));

    CREATE INDEX idx_orders_user_created ON orders (user_id, created_at DESC);

    CREATE INDEX idx_orders_open_by_symbol ON orders (symbol, created_at) WHERE status IN ('NEW', 'PARTIALLY_FILLED');