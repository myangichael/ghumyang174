DROP TABLE stocks;

CREATE TABLE stocks (
    symbol VARCHAR(10) PRIMARY KEY,
    current_price REAL,
    actor_name VARCHAR(30),
    dob DATE
);

CREATE TABLE stockclosingprices (
    symbol VARCHAR(30) NOT NULL,
    price REAL,
    record_date DATE,
    FOREIGN KEY (symbol) REFERENCES stocks(symbol) ON DELETE CASCADE
)

INSERT INTO stocks (symbol, current_price, actor_name, dob) VALUES ('SKB',40.00,'Kim Basinger',TO_DATE('1958/12/08', 'yyyy/mm/dd'));
INSERT INTO stocks (symbol, current_price, actor_name, dob) VALUES ('SMD',71.00,'Michael Douglas',TO_DATE('1944/09/25', 'yyyy/mm/dd'));
INSERT INTO stocks (symbol, current_price, actor_name, dob) VALUES ('STC',32.50,'Tom Cruise',TO_DATE('1962/07/03', 'yyyy/mm/dd'));

SELECT * FROM stocks;