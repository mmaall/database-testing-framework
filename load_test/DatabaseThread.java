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
            System.exit(-1);
        }

        // Prepare Statements

        String findCustomerByUID_str = "SELECT * FROM customers WHERE customer_id = ?";


        try{
            findCustomerByUID = conn.prepareStatement(findCustomerByUID_str);

        }
        catch(Exception e){
            System.err.println("ERROR: Unable to set up prepared statements");
            System.err.println(e.toString());
        }
    }

     
    public void run() {
        System.out.println("Running " +  threadName);
        Statement stmt = null;
        ResultSet rs = null;

        try {

            System.out.println("Test Select");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM customers");


            while (rs.next()){
                System.out.println(rs.getLong(1));
            }


            System.out.println("Test Prepared Statement");
            findCustomerByUID.setLong(1, 12883211224994746L);
            rs = findCustomerByUID.executeQuery();

            while (rs.next()){
                System.out.println(rs.getLong(1));
            }


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
