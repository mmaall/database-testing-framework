
import ordersapp.dbclients.DynamoClient;
import ordersapp.dbclients.DatabaseClientException;
import ordersapp.*;

import java.util.ArrayList;
import java.util.Date;

public class DynamoTesting {


    public static void main(String[] args) {

        int errorCount = 0;

        DynamoClient client = new DynamoClient("RandomTestTable", "us-east-1");

        System.out.println("Testing Customer Creates");

        for (int i = 0; i < 10; i++) {
            Customer customer = new Customer(i, "name" + i);

            for (int j = 0; j < i; j++) {
                customer.addAddress("addr" + j );
            }

            try {
                client.createCustomer(customer);
            } catch (DatabaseClientException e) {
                System.err.println("ERROR: Issues interacting with the database");
                System.err.println(e.getDetails());
                errorCount += 1;
            }
        }

        for (int i = 0; i < 10; i++) {

            Customer testCustomer = null;
            try {
                testCustomer = client.getCustomer(i);
                if (testCustomer.getUID() != i ) {
                    System.err.println("ERROR: Issue IDs don't match " + i);
                    errorCount += 1;
                }

                for (int j = 0; j < i; j++) {
                    ArrayList<String> addresses = testCustomer.getAddresses();

                    if (!addresses.get(j).equals("addr" + j)) {
                        System.err.println("ERROR: Uh oh, addresses aren't matching");
                        errorCount += 1;
                    }
                }
            } catch (DatabaseClientException e) {
                System.err.println("ERROR: Issues interacting with the database");
                System.err.println(e.getDetails());
                errorCount += 1;
                continue;
            }


        }

        System.out.println("Create Order Test");

        for (int i = 0; i < 10; i++){
            Order order = new Order(i, i, new Date(), "addr" + i);
            try{
                client.createOrder(order);
            } catch (DatabaseClientException e){
                System.err.println("ERROR: Issue creating an order");
                System.err.println(e.getDetails());
                errorCount +=1;
                continue;
            }
        }

        System.out.println("Exiting with " + errorCount + " errors");
    }
}