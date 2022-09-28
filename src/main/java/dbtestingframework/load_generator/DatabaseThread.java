
package load_generator;

import java.sql.*;
import java.util.ArrayList;
import data_generator.*;
import java.lang.Math;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;


import ordersapp.dbclients.DatabaseClient;
import ordersapp.dbclients.DatabaseClientException;
import ordersapp.*;
import data_generator.DataGenerator;
import data_generator.helpers.StringGenerator;

public class DatabaseThread extends Thread {

    private DatabaseClient dbClient;
    private DataGenerator dataGenerator;
    // Name of the thread
    private String threadName;
    private int count;
    // Holds how long the thread will run queries for in milliseconds
    private long threadRuntime;

    private int numEpochs;

    private TransactionInfo txnInfo;

    //Constructor

    // Thread runtime is in milliseconds
    // Minimum thread runtime is a minute 
    public DatabaseThread(String name, DatabaseClient dbClient, DataGenerator dataGenerator, long threadRuntime) {

        this.threadName = name;
        this.dbClient = dbClient;
        this.dataGenerator = dataGenerator;
        this.threadRuntime = threadRuntime;

        numEpochs = (int) (threadRuntime / 1000 / 60);

    }


    // TODO: Can extract a lot of the timing code into a single
    // method. Pass in method to invoke and args andtime 
    // the operation 
    public void run() {
        System.out.println("Running " + threadName);



        // Set up necessary timing information 
        long threadStartTime = System.currentTimeMillis();
        long threadEndTime = threadStartTime + threadRuntime;
        long totalTransactionTime = 0;
        int numTransactions = 0;
        txnInfo = new TransactionInfo(System.currentTimeMillis(), numEpochs);

        long[] startingUIDs = dataGenerator.getStartingUIDs();
        int[] recordCreateCount = dataGenerator.getRecordsCreatedCount();

        long customerStartUID = startingUIDs[0];
        long orderStartUID = startingUIDs[1];
        int totalCustomers = recordCreateCount[0];
        int totalOrders = recordCreateCount[1];

        System.out.println("Transactions starting");
        // Run this thread for the predetirmined amount of time.
        while (System.currentTimeMillis() < threadEndTime) {
            // Let's start executing queries

            // Generate a number between 1 and 100
            int randomInt = (int) (Math.random() * 100);


            // Get a customers information 
            if (randomInt < 40) {

                long customerUID = customerStartUID + ((int) Math.random() * totalCustomers);

                long txnStartTime = System.currentTimeMillis();

                try{
                    dbClient.getCustomer(customerUID);
                } catch (DatabaseClientException e){
                    System.err.println(e.getDetails());
                }

                long txnEndTime = System.currentTimeMillis();
                
                txnInfo.addTransaction(txnStartTime, txnEndTime);
                long txnTime = txnEndTime - txnStartTime;
                totalTransactionTime += txnTime;
                numTransactions++;

            } else if (randomInt < 80) {
                // get recentItems


                long customerUID = customerStartUID + ((int) Math.random() * totalCustomers);
                Date now = new Date();

                long txnStartTime = System.currentTimeMillis();

                try{
                    dbClient.getRecentItems(customerUID, now);
                } catch (DatabaseClientException e){
                    System.err.println(e.getDetails());
                }

                long txnEndTime = System.currentTimeMillis();
                
                txnInfo.addTransaction(txnStartTime, txnEndTime);
                long txnTime = txnEndTime - txnStartTime;
                totalTransactionTime += txnTime;
                numTransactions++;


            } else if (randomInt < 95){
                // create an order with some items

                int maxItems = 5;

                // Construct objects to be written
                long orderUID = UUID.randomUUID().getLeastSignificantBits();
                long customerUID = customerStartUID + ((int) Math.random() * totalCustomers);
                Date currentTime = new Date();
                Order newOrder = new Order(
                    orderUID,
                    customerUID,
                    currentTime,
                    StringGenerator.generateAlphaNumeric(40, 5)
                    );

                Item[] items = new Item[maxItems];
                int itemWriteCount = (int) Math.random() * maxItems; 

                for(int i = 0; i < itemWriteCount; i++) {
                    long itemUID = UUID.randomUUID().getLeastSignificantBits(); 
                    items[i] = new Item(
                        itemUID,
                        orderUID,
                        customerUID,
                        (int) Math.random()*5,
                        currentTime
                        );
                }

                // Run the writes
                long txnStartTime = System.currentTimeMillis();

                try{
                    dbClient.createOrder(newOrder);
                } catch (DatabaseClientException e){
                    System.err.println(e.getDetails());
                }

                long txnEndTime = System.currentTimeMillis();
                
                txnInfo.addTransaction(txnStartTime, txnEndTime);
                long txnTime = txnEndTime - txnStartTime;
                totalTransactionTime += txnTime;
                numTransactions++;

                // Write items if necessary 
                for (int i = 0; i < itemWriteCount; i++){
                    txnStartTime = System.currentTimeMillis();

                    try{
                        dbClient.createItem(items[i]);
                    } catch (DatabaseClientException e){
                        System.err.println(e.getDetails());
                    }

                    txnEndTime = System.currentTimeMillis();
                    
                    txnInfo.addTransaction(txnStartTime, txnEndTime);
                    txnTime = txnEndTime - txnStartTime;
                    totalTransactionTime += txnTime;
                    numTransactions++; 
                } 

            } else {
                // create a new customer

                Customer newCustomer = new Customer(
                        UUID.randomUUID().getLeastSignificantBits(),
                        StringGenerator.generateAlphaNumeric(14, 2)
                    );
                newCustomer.addAddress(StringGenerator.generateAlphaNumeric(40,10));

                long txnStartTime = System.currentTimeMillis();

                try{
                    dbClient.createCustomer(newCustomer);
                } catch (DatabaseClientException e){
                    System.err.println(e.getDetails());
                }

                long txnEndTime = System.currentTimeMillis();
                
                txnInfo.addTransaction(txnStartTime, txnEndTime);
                long txnTime = txnEndTime - txnStartTime;
                totalTransactionTime += txnTime;
                numTransactions++;

            }
        }

        //System.out.println("Number of transactions: " + numTransactions);
        //System.out.println("Total time in transactions: "+ totalTransactionTime);

        double avgTxnTime = totalTransactionTime / numTransactions;

        /*
        System.out.println("Average Transaction Time: "+ avgTxnTime);
        System.out.println("Transactional info from object");
        System.out.println(txnInfo);
        */
        System.out.println("Thread " +  threadName + " exiting.");
    }


    public TransactionInfo getTransactionInfo() {
        return txnInfo;
    }
}
