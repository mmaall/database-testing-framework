
package ordersapp.dbclients;

import java.util.ArrayList;
import java.util.Date;
import ordersapp.*;


// An interface to define database clients that implement the
// database interactions with a customer ordering system style
// system. 
public interface DatabaseClient {

    // Customer access
    void createCustomer(Customer customer) throws DatabaseClientException;

    Customer getCustomer(long uid) throws DatabaseClientException;
    //ArrayList<Order> getOrders(long customerUID);

    // Orders
    void createOrder(Order order) throws DatabaseClientException;
    Order getOrder(long orderUID, long customerUID) throws DatabaseClientException;

    // Line Items
    void createItem(Item item) throws DatabaseClientException;
    //ArrayList<Item> getItems(long orderUID) throws DatabaseClientException;
    ArrayList<Item> getRecentItems(long customerUID, Date date) throws DatabaseClientException;
}