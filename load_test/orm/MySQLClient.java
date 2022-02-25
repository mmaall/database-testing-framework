
package orm;

import java.util.ArrayList;
import java.sql.*;


// Client to access MySQL Database
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



        return new Customer();
    }

    public ArrayList<Order> getCustomerOrders(long uid){

        ArrayList<Order> orders = new ArrayList<Order>();

        return orders;
    }
}

