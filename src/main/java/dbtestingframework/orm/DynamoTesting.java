
import orm.*;

public class DynamoTesting {
    
    public static void main(String[] args){

        DynamoClient client = new DynamoClient("RandomTestTable", "us-east-1");

        for (int i = 0; i< 10; i++){
            Customer customer = new Customer(i, "name"+i);
            
            for (int j = 0; j < i; j++){
                customer.addAddress("addr" + j );
            }


            try {
                client.createCustomer(customer);
            } catch (DatabaseClientException e){
                System.err.println("Issues interacting with the database");
                System.err.println(e.getDetails());
            }
        }

    }
}