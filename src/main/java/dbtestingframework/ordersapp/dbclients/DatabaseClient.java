
package ordersapp.dbclients;

import java.util.ArrayList;
import ordersapp.*;

public interface DatabaseClient {

    // Customer access
    void createCustomer(Customer customer) throws DatabaseClientException;

    Customer getCustomer(long uid) throws DatabaseClientException;
    //ArrayList<Order> getOrders(long customerUID);

    // Orders
    void createOrder(Order order) throws DatabaseClientException;
    //void getOrder(long orderUID, long customerUID);
}