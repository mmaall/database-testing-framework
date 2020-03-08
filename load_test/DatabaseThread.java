import java.sql.*;



class DatabaseThread extends Thread{
    // Name of the thread  
    private String threadName;
    private int count;
    private String dbUrl;
    private Connection conn;
    private final String dbName = "rocks_db_test_db"; 


    private PreparedStatement findCustomerByUID; 


    //Constructor  
    DatabaseThread(String name, String databaseUrl) {
        System.out.println("Creating " +  name);
        threadName = name;
        dbUrl = databaseUrl; 
       
        // Set up connection
        try{
            conn = DriverManager.getConnection(dbUrl);
        }
        catch(SQLException e){
            System.err.println("ERROR: Unable to connect to " + dbUrl);
            System.err.println(e.toString());
        }

        // Statement test
         
    }

     
    public void run() {
        System.out.println("Running " +  threadName);
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM customers");


            while (rs.next()){
                System.out.println(rs.getLong(1));
            }
            // or alternatively, if you don't know ahead of time that
            // the query will be a SELECT...

            // Now do something with the ResultSet ....
        }
        catch (SQLException ex){
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }


        System.out.println("Thread " +  threadName + " exiting.");
    }


    public int getCount(){
        return count;
    }
} 
