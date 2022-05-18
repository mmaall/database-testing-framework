
package load_generator;

import java.sql.*;
import java.util.ArrayList;
import data_generator.*;
import java.lang.Math;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

import ordersapp.dbclients.DatabaseClient;
import ordersapp.dbclients.DatabaseClientException;
import data_generator.DataGenerator;


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
            System.out.println("Executing transaction");
            // Let's start executing queries

            // Generate a number between 1 and 100
            int randomInt = (int) (Math.random() * 100);


            // Get a customers information 
            if (randomInt < 30) {
                System.out.println("Get customer info");

                // Prepare
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
                System.out.println("Transaction time: " +txnTime);
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

    /**
    * Borrowed this from the internet
    * Wasn't in the mood to write it myself. Thanks internet, you do great.
    * src: https://www.logicbig.com/how-to/code-snippets/jcode-java-random-random-dates.html
    *
    **/

    public static int createRandomIntBetween(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }

    public static LocalDate createRandomDate(int startYear, int endYear) {
        int day = createRandomIntBetween(1, 28);
        int month = createRandomIntBetween(1, 12);
        int year = createRandomIntBetween(startYear, endYear);
        return LocalDate.of(year, month, day);
    }
}
