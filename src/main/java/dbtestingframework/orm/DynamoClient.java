
package orm;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.regions.Regions;
import com.amazonaws.regions.Region;
import java.util.HashMap;
import java.util.Map;

public class DynamoClient implements DatabaseClient{

    private String tableName;
    private AmazonDynamoDB ddb;

    public DynamoClient(){

    }

    public DynamoClient(String tableName){
        this.tableName = tableName;
        this.ddb = AmazonDynamoDBClient.builder()
            .withRegion(Regions.US_EAST_1)
            .build();
    }

    public DynamoClient(String tableName, String region){
        this.tableName = tableName;
        this.ddb = AmazonDynamoDBClient.builder()
            .withRegion(Regions.fromName(region))
            .build();
    }

    public void createCustomer(Customer customer) throws DatabaseClientException{

        HashMap<String,AttributeValue> itemValues =
            new HashMap<String,AttributeValue>();

        itemValues.put("pk", new AttributeValue(getCustomerPK(customer.getUID())));
        itemValues.put("sk", new AttributeValue(getCustomerSK(customer.getUID())));
        itemValues.put("name", new AttributeValue(customer.getName()));
        if (customer.getAddresses().size() > 0 ){
            itemValues.put("addresses", new AttributeValue(customer.getAddresses()));
        }

        try {
            this.ddb.putItem(this.tableName, itemValues);
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            throw new DatabaseClientException(e.getMessage());
        }
    }

    private String getCustomerPK(long uid){
        return "customer#" + String.valueOf(uid);
    }
    private String getCustomerSK(long uid){
        return getCustomerPK(uid);
    }
}
