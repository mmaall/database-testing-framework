
package load_generator;

import java.sql.*;
import java.util.ArrayList;
import data_generator.*;
import java.util.Random;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class DatabaseThread extends Thread {
    // Name of the thread
    private String threadName;
    private int count;
    private String dbUrl;
    private Connection conn;
    private final String dbName = "rocks_db_test_db";
    // Holds how long the thread will run queries for in milliseconds
    private long threadRuntime;

    private byte threadID;
    private byte systemID;

    private RecordInfo recordInfo;

    private int numEpochs;

    private TransactionInfo txnInfo;


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

    private PreparedStatement insertOrder;


    //Constructor

    // Thread runtime is in milliseconds
    public DatabaseThread(String name, String databaseUrl, RecordInfo recordInfo, byte systemID, byte threadID, long threadRuntime) {

        System.out.println("Creating " +  name);
        threadName = name;
        dbUrl = databaseUrl;
        this.threadID = threadID;
        this.systemID = systemID;
        this.recordInfo = recordInfo;
        this.threadRuntime = threadRuntime;

        numEpochs = (int) (threadRuntime / 1000 / 60);

        // Set up connection
        try {
            System.out.println("Attempting connection to " + databaseUrl);
            conn = DriverManager.getConnection(dbUrl);
            // Turning off autocommit
            conn.setAutoCommit(false);
            System.out.println("DB Connection Acquired");
        } catch (SQLException e) {
            System.err.println("ERROR: DatabaseThread: Unable to connect to " + dbUrl);
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

        String findProductBetweenDate_str = "SELECT * FROM products WHERE posting_date >= ? AND posting_date <= ?";

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

        String insertOrder_str = "INSERT INTO orders (order_id, customer_id, product_id, quantity) VALUES (?, ?, ?, ?)";


        try {

            // Customers
            findCustomerByUID = conn.prepareStatement(findCustomerByUID_str);
            findCustomerByAge = conn.prepareStatement(findCustomerByAge_str);

            // Products
            findProductByUID =  conn.prepareStatement(findProductByUID_str);
            findProductByPriceRange = conn.prepareStatement(findProductByPriceRange_str);
            findProductBeforeDate = conn.prepareStatement(findProductBeforeDate_str);
            // findProductAfterDate = conn.prepareStatement(findProductAfterDate_str);
            findProductBetweenDate = conn.prepareStatement(findProductBetweenDate_str);
            findProductByInventoryRange = conn.prepareStatement(findProductByInventoryRange_str);
            updateProductByID = conn.prepareStatement(updateProductByID_str);

            // Orders
            findOrderByUID = conn.prepareStatement(findOrderByUID_str);
            findOrderByCustomer = conn.prepareStatement(findOrderByCustomer_str);
            findOrderByProduct = conn.prepareStatement(findOrderByProduct_str);
            findOrderBeforeDate = conn.prepareStatement(findOrderBeforeDate_str);
            // findOrderAfterDate = conn.prepareStatement(findOrderAfterDate_str);
            // findOrderBetweenDate = conn.prepareStatement(findOrderBeforeDate_str);
            insertOrder = conn.prepareStatement(insertOrder_str);
            System.out.println("All statements prepared");
        } catch (Exception e) {
            System.err.println("ERROR: DatabaseThread: Unable to set up " +
                               "prepared statements");
            System.err.println(e.toString());
        }
    }


    public void run() {
        System.out.println("Running " + threadName);

        // Test query

        //System.exit(-1);


        // UID information holders, holds uids and is a queue. We will be making requests from the queue.

        int bufferSize = 250;

        ArrayList<Long> customerQueue = new ArrayList<Long>();

        ArrayList<Long> productQueue = new ArrayList<Long>();

        ArrayList<Long> orderQueue = new ArrayList<Long>();

        // UniqueID Generator

        UniqueIDGenerator uidGenerator = null;
        try {
            uidGenerator = new UniqueIDGenerator(systemID, threadID);
        } catch (Exception e) {
            System.err.println(e.toString());
            return;
        }
        System.out.println("UniqueIDGenerator Generated");


        // Let's do some real stuff
        long threadStartTime = System.currentTimeMillis();

        long threadEndTime = threadStartTime + threadRuntime;

        long totalTransactionTime = 0;

        int numTransactions = 0;

        txnInfo = new TransactionInfo(System.currentTimeMillis(), numEpochs);
        System.out.println("Transactions starting");
        // Run this thread for the predetirmined amount of time.
        while (System.currentTimeMillis() < threadEndTime) {
            System.out.println("Executing transaction");
            // Let's start executing queries

            // Generate a number between 1 and 100
            int randomInt = (int) (Math.random() * 100);


            // Get a customer's information, get orders for that customer
            // Uses the hot records provided on this machine
            if (randomInt < 25) {
                System.out.println("Get customer info, order something");
                int randomVal = (int) (Math.random() * 2);
                //System.out.println("Rand val: " + randomVal);
                long uid;

                // pull from the queue of ones we've quiered upon
                if (randomVal == 0 && customerQueue.size() > 0 ) {
                    //System.out.println("Random value");
                    uid = customerQueue.remove(0);
                }
                // Get from the hot records
                else {
                    uid = recordInfo.getCustomerUID();
                }



                try {
                    // Set up the prepared statements
                    findCustomerByUID.setLong(1, uid);
                    findOrderByCustomer.setLong(1, uid);

                    long txnStartTime = System.currentTimeMillis();
                    // Execute query
                    ResultSet rset = findCustomerByUID.executeQuery();
                    if (rset.next()) {
                        //System.out.println("ID Found: " + rset.getLong(1));
                    }

                    rset = findOrderByCustomer.executeQuery();

                    // Add the orders to orders queue
                    while (rset.next()) {
                        // enforce buffer size
                        if (orderQueue.size() < bufferSize ) {
                            orderQueue.add(rset.getLong(1));
                        }

                        if (productQueue.size() < bufferSize) {
                            //System.out.println("Add to product queue");
                            productQueue.add(rset.getLong(3));
                            //System.out.println("Product queue size:"+ productQueue.size());
                        }

                    }

                    conn.commit();

                    long txnEndTime = System.currentTimeMillis();
                    txnInfo.addTransaction(txnStartTime, txnEndTime);
                    long txnTime = txnEndTime - txnStartTime;
                    totalTransactionTime += txnTime;
                    numTransactions++;
                } catch (SQLException e) {
                    System.err.println("ERROR: DatabaseThread: Sql exception during query.");
                    System.err.println(e.toString());
                }

            }

            // Increase the inventory of an item
            else if (randomInt >= 98) {
                System.out.println("Increase inventory");

                int randomVal = (int) (Math.random() * 2);

                long uid;

                // pull from the queue of ones we've quiered upon
                if (randomVal == 0 && productQueue.size() > 0 ) {
                    //System.out.println("Selecting from product queue");
                    uid = productQueue.remove(0);
                }
                // Get from the hot records
                else {
                    uid = recordInfo.getProductUID();
                }


                try {
                    int amountToAdd = (int) (Math.random() * 1000);

                    updateProductByID.setInt(1, amountToAdd);
                    updateProductByID.setLong(2, uid);

                    long txnStartTime = System.currentTimeMillis();
                    int numUpdates = updateProductByID.executeUpdate();
                    conn.commit();
                    long txnEndTime = System.currentTimeMillis();
                    txnInfo.addTransaction(txnStartTime, txnEndTime);
                    long txnTime = txnEndTime - txnStartTime;
                    totalTransactionTime += txnTime;
                    numTransactions++;

                } catch (SQLException e) {
                    System.err.println("ERROR: DatabaseThread: Sql exception during query.");
                    System.err.println(e.toString());
                }

            }

            // Get customer info
            // Get info about 10 products
            // Order one of those products
            else if (25 <= randomInt && randomInt < 75) {

                System.out.println("Customer info, ask about 10 products, order one");
                int randomVal = (int) (Math.random() * 2);
                //System.out.println("Rand val: " + randomVal);
                long customerUID;

                // pull from the queue of ones we've quiered upon
                if (randomVal == 0 && customerQueue.size() > 0 ) {
                    //System.out.println("Random value");
                    customerUID = customerQueue.remove(0);
                }
                // Get from the hot records
                else {
                    customerUID = recordInfo.getCustomerUID();
                }


                long[] productIds = new long[10];
                for (int i = 0; i < productIds.length; i++) {

                    randomVal = (int) (Math.random() * 2);

                    if (randomVal == 0 && productQueue.size() > 0) {
                        productIds[i] = productQueue.remove(0);
                    } else {
                        productIds[i] = recordInfo.getProductUID();
                    }

                }

                int amountToAdd = (int) (Math.random() * 100);
                long productToAdd =
                    productIds[(int) (Math.random() * productIds.length)];
                // Generate the UID for the order


                long orderUID = uidGenerator.getUID();

                // Prepare the queries we can
                try {
                    findCustomerByUID.setLong(1, customerUID);

                    updateProductByID.setInt(1, -1 * amountToAdd);
                    updateProductByID.setLong(2, productToAdd);

                    insertOrder.setLong(1, orderUID);
                    insertOrder.setLong(2, customerUID);
                    insertOrder.setLong(3, productToAdd);
                    insertOrder.setInt(4, amountToAdd);

                    // Start executing
                    long txnStartTime = System.currentTimeMillis();
                    // Get the customer info
                    ResultSet rset = findCustomerByUID.executeQuery();
                    // Get the info about all those good products
                    for (int i = 0; i < productIds.length; i++) {
                        findProductByUID.setLong(1, productIds[i]);
                        rset = findProductByUID.executeQuery();
                    }

                    // Update the product to remove the quantity to order
                    updateProductByID.executeUpdate();

                    // Insert the order
                    insertOrder.executeUpdate();


                    conn.commit();

                    long txnEndTime = System.currentTimeMillis();
                    txnInfo.addTransaction(txnStartTime, txnEndTime);
                    long txnTime = txnEndTime - txnStartTime;
                    totalTransactionTime += txnTime;
                    numTransactions++;

                } catch (SQLException e) {
                    System.err.println("ERROR: DatabaseThread: Sql exception during query.");
                    System.err.println(e.toString());
                }


            }

            /*
            // Get the products in a date range
            else if(75<= randomInt && randomInt < 98){
                System.out.println("Product id by date range");
                String date1 = createRandomDate(2000, 2010).toString();
                String date2 = createRandomDate(2010, 2020).toString();

                try{

                    findProductBetweenDate.setString(1, date1);
                    findProductBetweenDate.setString(2, date2);

                    // Start executing
                    long txnStartTime = System.currentTimeMillis();
                    // Get the customer info
                    ResultSet rset = findProductBetweenDate.executeQuery();

                    while(rset.next()){
                        if (productQueue.size() < bufferSize){
                            productQueue.add(rset.getLong(1));
                        }
                    }
                    conn.commit();

                    long txnEndTime = System.currentTimeMillis();
                    txnInfo.addTransaction(txnStartTime, txnEndTime);
                    long txnTime = txnEndTime - txnStartTime;
                    totalTransactionTime += txnTime;
                    numTransactions++;

                    System.out.println(totalTransactionTime);

                }
                catch(SQLException e){
                    System.err.println("ERROR: DatabaseThread: Sql exception during query.");
                    System.err.println(e.toString());
                }


            }
            */

        }



        //System.out.println("Number of transactions: " + numTransactions);
        //System.out.println("Total time in transactions: "+ totalTransactionTime);

        double avgTxnTime = totalTransactionTime / numTransactions;

        /*
        System.out.println("Average Transaction Time: "+ avgTxnTime);
        System.out.println("Transactional info from object");
        System.out.println(txnInfo);
        */
        System.out.println("Thread " +  threadName + " exiting.");
    }


    public TransactionInfo getTransactionInfo() {
        return txnInfo;
    }

    public int getCount() {
        return count;
    }

    /**
    * Borrowed this from the internet
    * Wasn't in the mood to write it myself. Thanks internet, you do great.
    * src: https://www.logicbig.com/how-to/code-snippets/jcode-java-random-random-dates.html
    *
    **/

    public static int createRandomIntBetween(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }

    public static LocalDate createRandomDate(int startYear, int endYear) {
        int day = createRandomIntBetween(1, 28);
        int month = createRandomIntBetween(1, 12);
        int year = createRandomIntBetween(startYear, endYear);
        return LocalDate.of(year, month, day);
    }
}
