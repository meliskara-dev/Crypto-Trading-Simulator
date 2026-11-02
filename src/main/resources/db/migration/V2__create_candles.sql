CREATE TABLE candles(
    id        UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol    VARCHAR(20)    NOT NULL,
    timeframe VARCHAR(5)     NOT NULL,
    open_time TIMESTAMPTZ      NOT NULL,
    open      NUMERIC(38,18) NOT NULL,
    high      NUMERIC(38,18) NOT NULL,
    low       NUMERIC(38,18) NOT NULL,
    close     NUMERIC(38,18) NOT NULL,
    UNIQUE (symbol, timeframe, open_time),
    CHECK (low <= high)
);