CREATE TABLE trades (
    id            UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol        VARCHAR(20)     NOT NULL,
    buy_order_id  UUID            NOT NULL REFERENCES orders(id),
    sell_order_id UUID            NOT NULL REFERENCES orders(id),
    buyer_id      UUID            NOT NULL REFERENCES users(id),
    seller_id     UUID            NOT NULL REFERENCES users(id),
    taker_side    VARCHAR(4)      NOT NULL,
    price         NUMERIC(38, 18) NOT NULL,
    quantity      NUMERIC(38, 18) NOT NULL,
    executed_at   TIMESTAMPTZ     NOT NULL,

    CONSTRAINT ck_trades_taker_side CHECK (taker_side IN ('BUY', 'SELL')),
    CONSTRAINT ck_trades_price      CHECK (price > 0),
    CONSTRAINT ck_trades_quantity   CHECK (quantity > 0)
);

CREATE INDEX idx_trades_buy_order ON trades (buy_order_id);
CREATE INDEX idx_trades_sell_order ON trades (sell_order_id);