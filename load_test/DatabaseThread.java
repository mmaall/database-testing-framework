class DatabaseThread extends Thread{
    // Name of the thread  
    private String threadName;
    private int count;

    //Constructor  
    DatabaseThread( String name) {
        threadName = name;
        System.out.println("Creating " +  threadName );
    }

     
    public void run() {
        System.out.println("Running " +  threadName );
        try {
            for(int i = 4; i > 0; i--) {
                System.out.println("Thread: " + threadName + ", " + i);
                //Add to our count  
                count++; 
                // Let the thread sleep for a while.
                Thread.sleep(1);
            }
        }

        catch (InterruptedException e) {
            System.out.println("Thread " +  threadName + " interrupted.");
        }

        System.out.println("Thread " +  threadName + " exiting.");
    }


    public int getCount(){
        return count;
    }
} 
