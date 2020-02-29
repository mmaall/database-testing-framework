
CREATE TABLE customers(
    cid             INTEGER,
    name            VARCHAR,
    address         VARCHAR,
    age             INTEGER,
    PRIMARY KEY(cid)
);

CREATE TABLE products(
    pid             INTEGER,
    name            VARCHAR, 
    category        VARCHAR,
    price           DECIMAL(15,2),
    description     VARCHAR,
    posting_date    DATE 
    PRIMARY KEY (iid)
);

CREATE TABLE orders(
    oid             INTEGER,
    cid             INTEGER,
    pid             INTEGER,
    quantity        INTEGER,
    purchase_time   DATETIME,
    PRIMARY KEY (oid)

);