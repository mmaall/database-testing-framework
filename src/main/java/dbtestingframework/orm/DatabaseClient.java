
package orm;

import java.util.ArrayList;

public interface DatabaseClient{

    // Customer access 
    void createCustomer(Customer customer) throws DatabaseClientException;

    Customer getCustomer(long uid) throws DatabaseClientException;
    //ArrayList<Order> getOrders(long customerUID);

    // Orders 
    //void createOrder(Order order);
    //void getOrder(long orderUID, long customerUID);
}