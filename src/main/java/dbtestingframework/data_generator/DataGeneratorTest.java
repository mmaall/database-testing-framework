
import data_generator.DataGenerator;
import data_generator.DataGeneratorException;
import ordersapp.dbclients.DynamoClient;

public class DataGeneratorTest {
    
    public static void main(String[] args){

        DynamoClient ddbClient = new DynamoClient("RandomTestTable", "GSI1", "us-east-1");

        int[] weights = {33,33,33};
        long[] startingUIDs = {0,0,0};

        DataGenerator generator = null;
        try {
            generator = new DataGenerator(ddbClient, weights, startingUIDs);
        } catch (DataGeneratorException e) {
            System.err.println("ERROR: Unable to create data generator");
            System.err.println(e.getDetails());
        }

        generator.run(15);
    }
}