
package orm;

import java.util.ArrayList;

public interface DatabaseClient{

    // Customer access 
    void createCustomer(Customer customer);

    Customer getCustomer(long uid);
    ArrayList<Order> getCustomerOrders(long uid);

    // Item access
    //void getProduct(long uid);

    // Orders 
    //void getOrder(long uid);
    //void addOrder(long orderUID, long customerUID, long productUID, int amount);
}