

import load_generator.DatabaseThread;
import load_generator.TransactionInfo;
import data_generator.DataGenerator;
import data_generator.DataGeneratorException;
import ordersapp.dbclients.DynamoClient;


public class ThreadTester {

    public static void main(String[] args){

        int totalRecords = 24;

        DynamoClient ddbClient = new DynamoClient("RandomTestTable", "GSI1", "us-east-1");


        // Generate some data
        int[] weights = {33,33,33};
        long[] startingUIDs = {0,0,0};

        DataGenerator generator = null;
        try {
            generator = new DataGenerator(ddbClient, weights, startingUIDs);
        } catch (DataGeneratorException e) {
            System.err.println("ERROR: Unable to create data generator");
            System.err.println(e.getDetails());
        }

        int recordsCreated = generator.run(totalRecords);

        System.out.println(recordsCreated + " records created on the goal of " + totalRecords);


        // Run a database thread 
        System.out.println("Load Testing");
        DatabaseThread dbThread1 = new DatabaseThread("thread1", ddbClient, generator, 1000 * 60 * 5);
        dbThread1.start();

        // Sleep wait while the thread is still running 
        while(dbThread1.isAlive()){

            try{
                Thread.sleep(1000*1);
            } catch (Exception e){
                 System.err.println("ERROR: LoadDriver: Exception "+ 
                                "thrown trying to call Thread.sleep");
                System.err.println(e.toString());
            }
        }

        TransactionInfo txnInfo = dbThread1.getTransactionInfo();

        txnInfo.setTag("Michael's test");
        txnInfo.toJsonFile("simple-test.json", "simple-test-all.json");

    }

}