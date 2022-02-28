# Database Testing Framework

Aiming to test differences in scale between DynamoDB and some form of relational db. 


# Setup

Required packages
- JSON Simple: https://code.google.com/archive/p/json-simple/downloads
- MySQL Connector: https://dev.mysql.com/downloads/connector/j/
Upon downloading ensure they are added to the CLASSPATH.

# Usage


# Components

#### Scheduler 

Command line utility to start and aggregate the results of a database load testing job. 

#### Load Generators

These will generate load/transactions to the databse that is being tested. Lives in containers in ECS tasks and will be started/stopped by the scheduler. 

#### Data Generators

Generates synthetic data that is going to be used in the database being load tested. These will be started as containers in ECS tasks. This will be kicked off by the scheduler. 

# TODOS

## Write ORMs

## Write DB clients
- MySQL
- Dynamo

## Determine proportions
- How many TPS
    - Need to google
    - scale w/ data set size?
- Data set sizes
- Distribution of types of transactions
    - Customers should be uniform
    - Products should gausian/normal
    - How are we going to inject uids for customers, users, products to actually start 
