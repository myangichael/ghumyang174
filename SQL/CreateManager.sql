Name,username,password,STATE,Phone,email,TAXID
John Admin,admin,secret,CA,(805)6374632,admin@stock.com,000001000

CREATE TABLE managers (
    manager_id INTEGER PRIMARY KEY,
    username VARCHAR(30) UNIQUE,
    password VARCHAR(30),
    name VARCHAR(30),
    state VARCHAR(2),
    phone_number VARCHAR(10),
    email_address VARCHAR(30),
    tax_id VARCHAR(9)
);

INSERT INTO managers
(manager_id, username, password, name, state, phone_number, email_address, tax_id)
VALUES
(1, 'admin', 'secret','John Admin', 'CA', '8056374632', 'admin@stock.com', '000001000');