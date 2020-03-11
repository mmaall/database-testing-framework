

class TestDriver{

    public static void main(String args[]){
        



        // Database info

        String databaseUrl = "jdbc:mysql://localhost/rocks_db_test_db";
        String userName = "michael";
        String password = "m2lanthier";
        String infoFile = "./miniSelectedIDs.json";

        String fullUrl = databaseUrl+"?user="+userName+"&password="+password;


        RecordInfo selectedRecordInfo = new RecordInfo(infoFile);

        DatabaseThread dbThread1 = new DatabaseThread("thread1", fullUrl, selectedRecordInfo, (byte) 0, (byte) 0, 120 * 1000);
        dbThread1.start();
        System.out.println("Is thread alive? " + dbThread1.isAlive());

        DatabaseThread dbThread2 = new DatabaseThread("thread2", fullUrl, selectedRecordInfo, (byte) 0, (byte) 1, 120 *1000);
        dbThread2.start();



        while (true){
            if (!dbThread1.isAlive() && !dbThread2.isAlive()){
                System.out.printf("Count: %d\n", dbThread1.getCount());       
                break;
            }
        }

        // Threads should have ended
        TransactionInfo txnInfo = dbThread1.getTransactionInfo();
        TransactionInfo txnInfo2 = dbThread2.getTransactionInfo();
        
        System.out.println("Thread 1 info\n "+txnInfo.toString());

        System.out.println("Thread 2 info\n "+txnInfo2.toString());


        txnInfo.combine(txnInfo2);

        System.out.println("Combined: \n" + txnInfo.toString()); 
    }
}