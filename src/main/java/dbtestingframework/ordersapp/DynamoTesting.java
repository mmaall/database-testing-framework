
import ordersapp.dbclients.DynamoClient;
import ordersapp.dbclients.DatabaseClientException;
import ordersapp.*;

import java.util.ArrayList;
import java.util.Date;

public class DynamoTesting {


    public static void main(String[] args) {

        int errorCount = 0;

        DynamoClient client = new DynamoClient("RandomTestTable", "GSI1", "us-east-1");

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

            Date orderDate = new Date();
            Order newOrder = new Order(i, i, orderDate, "addr" + i);
            try{
                client.createOrder(newOrder);
            } catch (DatabaseClientException e){
                System.err.println("ERROR: Issue creating an order");
                System.err.println(e.getDetails());
                errorCount +=1;
                continue;
            }

            // Add some items to the order 
            for (int j = 0; j < 2; j++){
                Item newItem = new Item(j, i, i, 2, orderDate);
                try{
                    client.createItem(newItem);
                } catch (DatabaseClientException e){
                    System.err.println("ERROR: Issue creating an item for an order");
                    System.err.println(e.getDetails());
                    errorCount +=1;
                    continue;
                }
            }

            try{
                Order foundOrder = client.getOrder(i, i);

                if (foundOrder == null){
                    System.err.println("ERROR: Couldn't find order (" + i +","+i+ ")");
                    errorCount+=1;
                    continue;
                }

                if (!newOrder.getAddress().equals(foundOrder.getAddress())){
                    System.err.println("ERROR: Orders addresses not matching");
                    errorCount += 1;
                }
                else if (newOrder.getCreateDate().getTime() != foundOrder.getCreateDate().getTime()){
                    System.err.println("ERROR: Orders times not matching");
                    errorCount +=1; 
                }

            } catch (DatabaseClientException e){
                System.err.println("ERROR: Issue getting an order");
                System.err.println(e.getDetails());
                errorCount +=1;
                continue;
            }
        }

        System.out.println("Get Recent Line Item test");

        long cUID = 42;
        String cName = "Sir Orders a Lot";
        String cAddr = "weird address";
        Customer newCustomer = new Customer(cUID, cName);
        newCustomer.addAddress(cAddr);
        try{

            // Don't really need this b/c who cares about referential integrity
            client.createCustomer(newCustomer);

            for (int i = 0; i < 10; i ++){
                client.createItem(new Item(i, i, cUID, 2, new Date(i)));
            }

            ArrayList<Item> itemsFound = client.getRecentItems(cUID, new Date());

            if (itemsFound.size() != 5){
                System.err.println("ERROR: Expected 5 items from GSI, got " + itemsFound.size());
                errorCount +=1; 
            }

            for(int i = 1; i < itemsFound.size(); i ++){
                if (itemsFound.get(i).getOrderDate().getTime() > itemsFound.get(i-1).getOrderDate().getTime()){
                    System.err.println("Error: These look out of order");
                    errorCount +=1; 
                }
            }

        } catch (DatabaseClientException e){
            System.err.println("ERROR: Issue trying index scans");
            System.err.println(e.getDetails());
            errorCount +=1;
        }




        System.out.println("Exiting with " + errorCount + " errors");
    }
}