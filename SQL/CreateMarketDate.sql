DROP TABLE marketdate;

CREATE TABLE marketdate (
    market_date DATE
);

INSERT INTO marketdate VALUES (TO_DATE('10/16/2023', 'MM/DD/YYYY'));

SELECT TO_CHAR(M.market_date, 'YYYY-MM-DD') FROM marketdate M AS market_date;