DROP TABLE customers;
DROP SEQUENCE customer_id_seq;
DROP TRIGGER trigger_customer_id;

CREATE TABLE customers (
    customer_id INTEGER PRIMARY KEY,
    username VARCHAR(30) UNIQUE,
    password VARCHAR(30),
    name VARCHAR(30),
    state VARCHAR(2),
    phone_number VARCHAR(10),
    email_address VARCHAR(30),
    tax_id VARCHAR(9),
    balance REAL DEFAULT 0
);

CREATE SEQUENCE customer_id_seq;

CREATE TRIGGER trigger_customer_id
    BEFORE INSERT ON customers
    FOR EACH ROW
BEGIN
    SELECT customer_id_seq.nextval
        INTO :new.customer_id
        FROM dual;
END;

INSERT INTO Customers (name,username,password,state,phone_number,email_address,tax_id,balance) VALUES
('Alfred Hitchcock','alfred','hi','CA','8052574499','alfred@hotmail.com','000001022',10000);

INSERT INTO Customers (name,username,password,state,phone_number,email_address,tax_id,balance) VALUES
('Billy Clinton','billy','cl','CA','8055629999','billy@yahoo.com','000003045',100000);

INSERT INTO Customers (name,username,password,state,phone_number,email_address,tax_id,balance) VALUES
('Cindy Laugher','cindy','la','CA','8056930011','cindy@hotmail.com','000002034',50000);

INSERT INTO Customers (name,username,password,state,phone_number,email_address,tax_id,balance) VALUES
('David Copperfill','david','co','CA','8058240011','david@yahoo.com','000004093',45000);

INSERT INTO Customers (name,username,password,state,phone_number,email_address,tax_id,balance) VALUES
('Elizabeth Sailor','sailor','sa','CA','8051234567','sailor@hotmail.com','000001234',200000);

INSERT INTO Customers (name,username,password,state,phone_number,email_address,tax_id,balance) VALUES
('George Brush','brush','br','CA','8051357999','george@hotmail.com','000008956',5000);

INSERT INTO Customers (name,username,password,state,phone_number,email_address,tax_id,balance) VALUES
('Ivan Stock','ivan','st','NJ','8053223243','ivan@yahoo.com','000002341',2000);

INSERT INTO Customers (name,username,password,state,phone_number,email_address,tax_id,balance) VALUES
('Joe Pepsi','joe','pe','CA','8055668123','pepsi@pepsi.com','000000456',10000);

INSERT INTO Customers (name,username,password,state,phone_number,email_address,tax_id,balance) VALUES
('Magic Jordon','magic','jo','NJ','8054535539','jordon@jordon.org','000003455',130200);

INSERT INTO Customers (name,username,password,state,phone_number,email_address,tax_id,balance) VALUES
('Olive Stoner','olive','st','CA','8052574499','olive@yahoo.com','000001123',35000);

INSERT INTO Customers (name,username,password,state,phone_number,email_address,tax_id,balance) VALUES
('Frank Olson','frank','ol','CA','8053456789','frank@gmail.com','000003306',30500);

SELECT * FROM Customers;