package orm;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import java.util.ArrayList;


public class CustomerTest{

    @Test
    public void createCustomerDefault(){

        long uid = 1;
        String name = "John Smith";

        Customer customer = new Customer(uid, name);

        assertTrue(uid == customer.getUID());
        assertTrue(name.equals(customer.getName()));
        assertTrue(0 == customer.getAddresses().size());
    }

    @Test
    public void createCustomerArrayList(){

        long uid = 1;
        String name = "John Smith";
        ArrayList<String> addresses = new ArrayList<String>();
        addresses.add("309 E 6th St, Lordsburg, NM 88045");

        Customer customer = new Customer(uid, name, addresses);

        assertTrue(uid == customer.getUID());
        assertTrue(name.equals(customer.getName()));
        assertTrue(addresses.size() == customer.getAddresses().size());
    }


}