

import java.util.ArrayList;

class LoadDriver{

    public static void main(String args[]){
        

        // Argument information

        // [db_url, username, password, dataSize, testType, machineID, testLength, numThreads] 


        // Test length is in minutes
        // Database info


        /*
        String databaseUrl = args[0];
        String userName = args[1];
        String password = args[2];
        String testType = args[3];
        byte machineID = (byte) Integer.parseInt(args[3]);
        String infoFile = "./" + testType + "selectedIDs.json";
        // minutes, seconds, milliseconds
        long testLength = Integer.parseInt(args[4]) * 60 * 1000; 

        int numThreads = Integer.parseInt(args[5]);
        */


        String testType = "mini";
        String databaseUrl = "jdbc:mysql://localhost/rocks_db_test_db";
        String userName = "michael";
        String password = "m2lanthier";
        String infoFile = "./miniSelectedIDs.json";
        // minutes, seconds, milliseconds
        long testLength = 60 * 60 * 1000;
        int numThreads = 4; 
        byte machineID = (byte) 0;
        String fullUrl = databaseUrl+"?user="+userName+"&password="+password;





        RecordInfo selectedRecordInfo = new RecordInfo(infoFile);


        ArrayList<DatabaseThread> threadArray = new ArrayList<DatabaseThread>();

        for (int i = 0; i < numThreads; i++){
            String threadName = "thread"+i;
            threadArray.add(new DatabaseThread(threadName, fullUrl, selectedRecordInfo, machineID, (byte) i, testLength));
            threadArray.get(i).start();
        }

        /*
        DatabaseThread dbThread1 = new DatabaseThread("thread1", fullUrl, selectedRecordInfo, (byte) 0, (byte) 0, testLength);
        dbThread1.start();
        System.out.println("Is thread alive? " + dbThread1.isAlive());

        DatabaseThread dbThread2 = new DatabaseThread("thread2", fullUrl, selectedRecordInfo, (byte) 0, (byte) 1, testLength);
        dbThread2.start();
        */


        while (true){

            int doneThreads = 0;
            for(int i = 0; i < threadArray.size(); i++){
                // If the thread is dead, add it to done threads
                if (!threadArray.get(i).isAlive()){
                    doneThreads ++;
                }
            }

            if (doneThreads == numThreads){
                break;
            }

            // Check in 10 seconds
            try{ 
                Thread.sleep(1000*10);
            }
            catch (Exception e){
                System.err.println("ERROR: LoadDriver: Exception "+ 
                                "thrown trying to call Thread.sleep");
                System.err.println(e.toString());
            }
        }

        // Threads should have ended
        TransactionInfo txnInfo = threadArray.get(0).getTransactionInfo();

        for (int i = 1; i < threadArray.size(); i++ ){
            txnInfo.combine(threadArray.get(i).getTransactionInfo());
        }        

        txnInfo.setTag(testType);
        txnInfo.toJsonFile(testType+"Info1.json", testType+"Info2.json");

    }
}