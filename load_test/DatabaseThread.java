import java.sql.*;
import java.util.ArrayList;



class DatabaseThread extends Thread{
    // Name of the thread  
    private String threadName;
    private int count;
    private String dbUrl;
    private Connection conn;
    private final String dbName = "rocks_db_test_db"; 
    // Holds how long the thread will run queries for in seconds  
    private long threadRuntime = (long) (.25 * 60 * 1000); // minutes * seconds * milliseconds


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

    private PreparedStatement updateProductByID; 

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

        String findProductByUID_str = "SELECT * FROM products WHERE product_id = ?";

        String findProductByPriceRange_str = "SELECT * FROM products WHERE price >= ? AND price <= ?";

        String findProductBeforeDate_str = "SELECT * FROM products WHERE posting_date  <= ? ";

        String findProductAfterDate_str;

        String findProductBetweenDate_str; 

        String findProductByInventoryRange_str = "SELECT * FROM products WHERE quantity >= ? AND quantity <= ?"; 

        String updateProductByID_str = "UPDATE products SET quantity = quantity + ? WHERE product_id = ?";

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
            updateProductByID = conn.prepareStatement(updateProductByID_str); 

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

        // Test query
        long test_uid = recordInfo.getProductUID();
        System.out.println("UID: " + test_uid);
        
        try{
            int amountToAdd = (int) (Math.random() * 1000);
            System.out.println("Amount added: " + amountToAdd);
            updateProductByID.setInt(1, amountToAdd);
            updateProductByID.setLong(2, test_uid);

            System.out.println(updateProductByID.toString());
            int numUpdated = updateProductByID.executeUpdate();
            System.out.println("Rows updated: "+ numUpdated);
            conn.commit();

        }
        catch(SQLException e){
            System.err.println("ERROR: Sql exception during query.");
            System.err.println(e.toString());
        }

        System.exit(-1);

        // Let's do some real stuff
        long threadStartTime = System.currentTimeMillis();

        long threadEndTime = threadStartTime + threadRuntime; 

        long totalTransactionTime = 0;

        int numTransactions = 0; 


        // UID information holders




        // Run this thread for the predetirmined amount of time. 
        while(System.currentTimeMillis() < threadEndTime){

            // Let's start executing queries 

            // Generate a number between 1 and 100 
            int randomInt = (int) (Math.random()*100); 

            // Get a customer's information, get orders for that customer
            // Uses the hot records provided on this machine
            if(randomInt < 99){

                long uid = recordInfo.getCustomerUID();
                try{
                    // Set up the prepared statements
                    findCustomerByUID.setLong(1, uid);
                    findOrderByCustomer.setLong(1, uid);

                    long txnTime = System.currentTimeMillis(); 
                    // Execute query
                    ResultSet rset = findCustomerByUID.executeQuery();
                    if(rset.next()){
                        System.out.println("ID Found: " + rset.getLong(1));
                    }

                    rset = findOrderByCustomer.executeQuery();

                    /*
                    while(rset.next()){
                        System.out.println("Order ID: "+rset.getLong(1));
                    }
                    */


                    conn.commit();

                    txnTime = System.currentTimeMillis() - txnTime;
                    totalTransactionTime += txnTime;
                    numTransactions++;
                }
                catch(SQLException e){
                    System.err.println("ERROR: Sql exception during query.");
                    System.err.println(e.toString());
                }

            }

            // Increase the inventory of an item
            else if (randomInt >= 99){

                long uid = recordInfo.getProductUID();

                try{
                    int amountToAdd = (int) (Math.random() * 1000);

                    updateProductByID.setInt(1, amountToAdd);
                    updateProductByID.setLong(2, uid);

                    long txnTime = System.currentTimeMillis();
                    int numUpdates = updateProductByID.executeUpdate();
                    conn.commit();
                    txnTime = System.currentTimeMillis() - txnTime;
                    totalTransactionTime += txnTime;
                    numTransactions++; 

                }
                catch(SQLException e){
                    System.err.println("ERROR: Sql exception during query.");
                    System.err.println(e.toString());
                }

            }


        }

        System.out.println("Number of transactions: " + numTransactions);
        System.out.println("Total time in transactions: "+ totalTransactionTime);

        double avgTxnTime = totalTransactionTime / numTransactions;
        System.out.println("Average Transaction Time: "+ avgTxnTime);
        System.out.println("Thread " +  threadName + " exiting.");
    }


    public int getCount(){
        return count;
    }
} 
