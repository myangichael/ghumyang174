DROP TABLE Stocks;

CREATE TABLE Stocks (
    symbol VARCHAR(10) PRIMARY KEY,
    closing_price REAL,
    current_price REAL,
    actor_name VARCHAR(30),
    dob DATE
);

INSERT INTO Stocks (symbol, current_price, closing_price, actor_name, dob) VALUES ('SKB',40.00,40.00,'Kim Basinger',TO_DATE('1958/12/08', 'yyyy/mm/dd'));
INSERT INTO Stocks (symbol, current_price, closing_price, actor_name, dob) VALUES ('SMD',71.00,71.00,'Michael Douglas',TO_DATE('1944/09/25', 'yyyy/mm/dd'));
INSERT INTO Stocks (symbol, current_price, closing_price, actor_name, dob) VALUES ('STC',32.50,32.50,'Tom Cruise',TO_DATE('1962/07/03', 'yyyy/mm/dd'));

SELECT * FROM Stocks;