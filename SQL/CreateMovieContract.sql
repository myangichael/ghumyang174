DROP TABLE moviecontracts;

CREATE TABLE moviecontracts (
    symbol VARCHAR(10),
    movie_id INTEGER,
    contract_role VARCHAR(15),
    total_value REAL,
    PRIMARY KEY (symbol, movie_id),
    FOREIGN KEY (symbol) REFERENCES stocks,
    FOREIGN KEY (movie_id) REFERENCES movies
);

INSERT INTO moviecontracts (symbol,movie_id,contract_role,total_value) VALUES ('SKB', 1, 'Actor', 5000000);
INSERT INTO moviecontracts (symbol,movie_id,contract_role,total_value) VALUES ('SMD', 2, 'Actor', 10000000);
INSERT INTO moviecontracts (symbol,movie_id,contract_role,total_value) VALUES ('STC', 3, 'Actor', 5000000);