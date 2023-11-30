DROP TABLE balancehistories;

CREATE TABLE balancehistories (
    customer_id INTEGER,
    balance REAL,
    record_date DATE,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
)