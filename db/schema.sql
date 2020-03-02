
CREATE DATABASE IF NOT EXISTS rocks_db_test_db;

USE rocks_db_test_db;

CREATE TABLE customers(
    customer_id     INT PRIMARY KEY,
    name            VARCHAR(50),
    address         VARCHAR(125),
    age             INT
);

CREATE TABLE products(
    product_id      INT PRIMARY KEY,
    name            VARCHAR(100), 
    category        VARCHAR(50),
    price           DECIMAL(15,2),
    description     VARCHAR(5000),
    posting_date    DATE 
);

CREATE TABLE orders(
    order_id        INT PRIMARY KEY,
    customer_id     INT,
    product_id      INT,
    quantity        INT,
    purchase_time   DATETIME
);
