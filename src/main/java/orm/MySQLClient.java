
package orm;

import java.util.ArrayList;
import java.sql.*;


// Client to access necessary data within MySQL 
public class MySQLClient implements DatabaseClient{

    // Database information 
    private String jdbcString; 
    private Connection dbConnection; 
    private String dbName; 



    // Prepared statements 
    private PreparedStatement queryCustomerByUID;


    public MySQLClient(String dbUrl, String dbName, String username, String password){
        
        this.jdbcString = "jdbc:mysql://" + dbUrl + "?user=" + username + "&password=" + password;
        this.dbName = dbName;

        // Setup database connection 

        try{
            System.out.println("Establishing connection to " + jdbcString);

            dbConnection = DriverManager.getConnection(this.jdbcString);
            dbConnection.setAutoCommit(false);
            System.out.println("Connection established");
        }
        catch(SQLException e){
            System.err.println("ERROR: DatabaseThread: Unable to connect to " + this.jdbcString);
            System.err.println(e.toString());
            System.exit(-1);
        }

        // Initialize prepared statements
        try{
            queryCustomerByUID = dbConnection.prepareStatement(
                                    "SELECT * FROM customers WHERE customer_id = ?");
        }
        catch(Exception e){
            System.err.println("ERROR: DatabaseThread: Unable to set up "+
                                "prepared statements");
            System.err.println(e.toString());
        }
    }

    public void createCustomer(Customer customer){

    }

    public Customer getCustomer(long uid){

        // Supply UID to prepared statment 
        try{
            queryCustomerByUID.setLong(1, uid);
        }
        catch (SQLException e){
            System.err.println("ERROR: Issue setting " + uid + " in prepared statement");
            System.err.println(e.toString());
        }
       
        // Run query  
        ResultSet rset = null; 

        try{
            rset = queryCustomerByUID.executeQuery();
        }
        catch (SQLException e){
            System.err.println("ERROR: Issue running query for customer " + uid);
            System.err.println(e.toString());
        }


        // Unpack and create customer object 
        Customer customer = null;

        try {
            customer = new Customer(rset.getLong(1), rset.getString(2), rset.getString(3), rset.getInt(4));
        }
        catch (SQLException e){
            System.err.println("ERROR: Issue unpacking customer " + uid + " result set");
            System.err.println(e.toString());
        }

        return customer;
    }

    public ArrayList<Order> getCustomerOrders(long uid){

        ArrayList<Order> orders = new ArrayList<Order>();

        return orders;
    }
}

