DROP TABLE stockaccounts;

CREATE TABLE stockaccounts (
    stock_acc_id INTEGER,
    customer_id INTEGER NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    num_shares REAL DEFAULT 0,
    buy_price REAL,
    PRIMARY KEY (stock_acc_id),
    FOREIGN KEY (customer_id) REFERENCES customers,
    FOREIGN KEY (symbol) REFERENCES stocks
);

DROP SEQUENCE stockaccounts_id_seq;
DROP TRIGGER trigger_stockaccounts_id;

CREATE SEQUENCE stockaccounts_id_seq START WITH 32;

CREATE TRIGGER trigger_stockaccounts_id
    BEFORE INSERT ON stockaccounts
    FOR EACH ROW
BEGIN
    SELECT stockaccounts_id_seq.nextval
        INTO :new.stock_acc_id
        FROM dual;
END;

INSERT INTO stockaccounts (customer_id,stock_acc_id,num_shares,symbol)
    WITH s AS (
        SELECT 1,012,100,'SKB' FROM dual UNION ALL
        SELECT 2,013,500,'SMD' FROM dual UNION ALL
        SELECT 2,014,100,'STC' FROM dual UNION ALL
        SELECT 3,015,250,'STC' FROM dual UNION ALL
        SELECT 4,016,100,'SKB' FROM dual UNION ALL
        SELECT 4,017,500,'SMD' FROM dual UNION ALL
        SELECT 4,018,50,'STC' FROM dual UNION ALL
        SELECT 5,019,1000,'SMD' FROM dual UNION ALL
        SELECT 6,020,100,'SKB' FROM dual UNION ALL
        SELECT 7,021,300,'SMD' FROM dual UNION ALL
        SELECT 8,022,500,'SKB' FROM dual UNION ALL
        SELECT 8,023,100,'STC' FROM dual UNION ALL
        SELECT 8,024,200,'SMD' FROM dual UNION ALL
        SELECT 9,025,1000,'SKB' FROM dual UNION ALL
        SELECT 10,026,100,'SKB' FROM dual UNION ALL
        SELECT 10,027,100,'SMD' FROM dual UNION ALL
        SELECT 10,028,100,'STC' FROM dual UNION ALL
        SELECT 11,029,100,'SKB' FROM dual UNION ALL
        SELECT 11,030,200,'STC' FROM dual UNION ALL
        SELECT 11,031,100,'SMD' FROM dual
    )
SELECT * FROM s;

SELECT * FROM stockaccounts;

SELECT (symbol, num_shares, buy_price, current_price, (num_shares * current_price), ((num_shares * current_price) - (num_shares * buy_price)))
AS (symbol, num_shares, buy_price, current_price, current_value, change)
FROM stockaccounts A
INNER JOIN stocks S ON A.symbol=S.symbol
WHERE customer_id=12