
class TestDriver{

    public static void main(String args[]){
        DatabaseThread dbThread1 = new DatabaseThread("thread1");
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