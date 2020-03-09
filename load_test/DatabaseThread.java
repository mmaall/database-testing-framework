import java.sql.*;



class DatabaseThread extends Thread{
    // Name of the thread  
    private String threadName;
    private int count;
    private String dbUrl;
    private Connection conn;
    private final String dbName = "rocks_db_test_db"; 
    // Holds how long the thread will run queries for in seconds  
    private long threadRuntime = 60 * 60 * 1000; // minutes * seconds * milliseconds


    private RecordInfo recordInfo; 

    /**
     * Customer prepared statements 
    **/
    private PreparedStatement findCustomerByUID; 

    private PreparedStatement findCustomerByAge;

    /**
     * Product prepared statements
    **/

    private PreparedStatement findProductByUID;

    private PreparedStatement findProductByPriceRange;

    private PreparedStatement findProductBeforeDate;

    private PreparedStatement findProductAfterDate;

    private PreparedStatement findProductBetweenDate; 

    private PreparedStatement findProductByInventoryRange; 

    /**
     * Order prepared statements 
    **/

    private PreparedStatement findOrderByUID; 

    private PreparedStatement findOrderByCustomer;

    private PreparedStatement findOrderByProduct;

    private PreparedStatement findOrderBeforeDate;

    private PreparedStatement findOrderAfterDate;

    private PreparedStatement findOrderBetweenDate;



    //Constructor  
    DatabaseThread(String name, String databaseUrl, RecordInfo recordInfo) {
        System.out.println("Creating " +  name);
        threadName = name;
        dbUrl = databaseUrl;

        this.recordInfo = recordInfo; 
       
        // Set up connection
        try{
            conn = DriverManager.getConnection(dbUrl);
            // Turning off autocommit 
            conn.setAutoCommit(false);
        }
        catch(SQLException e){
            System.err.println("ERROR: Unable to connect to " + dbUrl);
            System.err.println(e.toString());
            System.exit(-1);
        }

        // Prepare Statements

        /**
         * Customer prepared statements 
        **/

        // Find a customer by their given UID 
        String findCustomerByUID_str = "SELECT * FROM customers WHERE customer_id = ?";
        // Find a customer by their age      
        String findCustomerByAge_str = "SELECT * FROM customers WHERE age >= ? AND age <= ?";

        /**
         * Product prepared statements
        **/

        String findProductByUID_str = "SELECT * FROM products WHERE prdouct_id = ?";

        String findProductByPriceRange_str = "SELECT * FROM products WHERE price >= ? AND price <= ?";

        String findProductBeforeDate_str = "SELECT * FROM products WHERE posting_date  <= ? ";

        String findProductAfterDate_str;

        String findProductBetweenDate_str; 

        String findProductByInventoryRange_str = "SELECT * FROM products WHERE quantity >= ? AND quantity <= ?"; 

        /**
         * Order prepared statements 
        **/

        String findOrderByUID_str = "SELECT * FROM orders WHERE order_id = ?"; 

        String findOrderByCustomer_str = "SELECT * FROM orders WHERE customer_id = ?";

        String findOrderByProduct_str = "SELECT * FROM orders WHERE product_id = ?";

        String findOrderBeforeDate_str = "SELECT * FROM orders WHERE purchase_time <= ?";

        String findOrderAfterDate_str;

        String findOrderBetweenDate_str;

        try{

            // Customers            
            findCustomerByUID = conn.prepareStatement(findCustomerByUID_str);
            findCustomerByAge = conn.prepareStatement(findCustomerByAge_str);

            // Products
            findProductByUID =  conn.prepareStatement(findProductByUID_str);
            findProductByPriceRange = conn.prepareStatement(findProductByPriceRange_str);
            findProductBeforeDate = conn.prepareStatement(findProductBeforeDate_str);
            // findProductAfterDate = conn.prepareStatement(findProductAfterDate_str);
            // findProductBetweenDate = conn.prepareStatement(findProductBetweenDate_str); 
            findProductByInventoryRange = conn.prepareStatement(findProductByInventoryRange_str); 

            // Orders
            findOrderByUID = conn.prepareStatement(findOrderByUID_str); 
            findOrderByCustomer = conn.prepareStatement(findOrderByCustomer_str);
            findOrderByProduct = conn.prepareStatement(findOrderByProduct_str);
            findOrderBeforeDate = conn.prepareStatement(findOrderBeforeDate_str);
            // findOrderAfterDate = conn.prepareStatement(findOrderAfterDate_str);
            // findOrderBetweenDate = conn.prepareStatement(findOrderBeforeDate_str);


        }
        catch(Exception e){
            System.err.println("ERROR: Unable to set up prepared statements");
            System.err.println(e.toString());
        }
    }

     
    public void run() {
        System.out.println("Running " + threadName);
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


        // Let's do some real stuff
        long threadStartTime = System.currentTimeMillis();

        long threadEndTime = threadStartTime + threadRuntime; 

        // Run this thread for the predetirmined amount of time. 
        while(System.currentTimeMillis() < threadEndTime){

            // Let's start executing queries 

            // Generate a number between 1 and 100 
            int randomInt = (int) (Math.random()*100); 

            // Get a customer's information
            if(randomInt <= 100){

                long uid = recordInfo.getCustomerUID();
                try{
                    findCustomerByUID.setLong(1, uid);
                    ResultSet rset = findCustomerByUID.executeQuery();
                    if(rset.next()){
                        System.out.println("ID Found: " + rset.getLong(1));
                    }
                }
                catch(SQLException e){
                    System.err.println("ERROR: Sql exception during query.");
                    System.err.println(e.toString());
                }

            }


        }



        System.out.println("Thread " +  threadName + " exiting.");
    }


    public int getCount(){
        return count;
    }
} 
