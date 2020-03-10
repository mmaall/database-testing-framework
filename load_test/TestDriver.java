
class TestDriver{

    public static void main(String args[]){
        



        // Database info

        String databaseUrl = "jdbc:mysql://localhost/rocks_db_test_db";
        String userName = "michael";
        String password = "m2lanthier";
        String infoFile = "./miniSelectedIDs.json";

        String fullUrl = databaseUrl+"?user="+userName+"&password="+password;


        RecordInfo selectedRecordInfo = new RecordInfo(infoFile);

        DatabaseThread dbThread1 = new DatabaseThread("thread1", fullUrl, selectedRecordInfo, 0, 0);
        dbThread1.start();
        System.out.println("Is thread alive? " + dbThread1.isAlive());
        
        while (true){
            if (!dbThread1.isAlive()){
                System.out.printf("Count: %d\n", dbThread1.getCount());       
                break;
            }
        }

    }
}