# Database Testing Framework

Aiming to test differences in scale between DynamoDB and some form of relational db. 


# Setup

Now using Maven so can manually run maven yourself or take advantage of the Makefile to build source code.

Infrastructure deployments can be done using the scheduler python script. 

# Usage


# Components

## Scheduler 

Command line utility to start and aggregate the results of a database load testing job. 

## Container Load Testers

These are HTTP Servers that hosts a Rest API to manage load testing. Can accept methods for generating data, starting load testing, getting load test information. Lives in containers in ECS tasks and will be started/stopped by the scheduler. 

### Load Testing
An HTTP POST by the scheduler with duration can trigger the container to generate load/transactions to the database that is being tested. 

## Data Generation
Generates synthetic data that is going to be used in the database being load tested. Triggered by an HTTP POST which includes the amount of data being generated as well as the db driver to use 

# TODOS

## Write ORMs

## Write DB clients
- MySQL
- Dynamo
    - Happy w/ secondary indexes
    - Less happy w/ scans 
    - Less happy w/ manual joins (maybe?)

## Determine proportions
- How many TPS
    - Need to google
    - scale w/ data set size?
- Data set sizes
- Distribution of types of transactions
    - Customers should be uniform
    - Products should gausian/normal
    - How are we going to inject uids for customers, users, products to actually start 
