DROP TABLE depositwithdrawal;
DROP TABLE buys;
DROP TABLE sells;
DROP TABLE cancels;
DROP TABLE accrueinterests;
DROP TABLE transactions;
DROP SEQUENCE transaction_id_seq;
DROP TRIGGER trigger_transaction_id;

CREATE TABLE transactions (
    transaction_id INTEGER,
    customer_id INTEGER,
    transaction_date DATE,
    PRIMARY KEY (transaction_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE depositwithdrawal (
    transaction_id INTEGER,
    amount REAL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

CREATE TABLE buys (
    transaction_id INTEGER,
    symbol VARCHAR(30),
    purchase_price REAL,
    num_shares REAL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    FOREIGN KEY (symbol) REFERENCES stocks(symbol)
);

CREATE TABLE sells (
    transaction_id INTEGER,
    symbol VARCHAR(30),
    purchase_price REAL,
    sell_price REAL,
    num_shares REAL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    FOREIGN KEY (symbol) REFERENCES stocks(symbol)
);

CREATE TABLE cancels (
    transaction_id INTEGER,
    transaction_canceled INTEGER,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

CREATE TABLE accrueinterests (
    transaction_id INTEGER,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

CREATE SEQUENCE transaction_id_seq;

CREATE TRIGGER trigger_transaction_id
    BEFORE INSERT ON transactions
    FOR EACH ROW
BEGIN
    SELECT transaction_id_seq.nextval
        INTO :new.transaction_id
        FROM dual;
END;