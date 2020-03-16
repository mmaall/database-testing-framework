USE rocks_db_test_db;



DROP INDEX customer_index on orders;

DROP TABLE customers CASCADE;

DROP TABLE products CASCADE;

DROP TABLE orders CASCADE;
