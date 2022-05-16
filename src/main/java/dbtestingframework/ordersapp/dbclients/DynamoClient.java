
package ordersapp.dbclients;

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
import java.util.Set;
import java.util.ArrayList;

import ordersapp.*;


public class DynamoClient implements DatabaseClient {

    private String tableName;
    private AmazonDynamoDB ddb;

    public DynamoClient() {

    }

    public DynamoClient(String tableName) {
        this.tableName = tableName;
        this.ddb = AmazonDynamoDBClient.builder()
                   .withRegion(Regions.US_EAST_1)
                   .build();
    }

    public DynamoClient(String tableName, String region) {
        this.tableName = tableName;
        this.ddb = AmazonDynamoDBClient.builder()
                   .withRegion(Regions.fromName(region))
                   .build();
    }

    public void createCustomer(Customer customer) throws DatabaseClientException {

        HashMap<String, AttributeValue> itemValues =
            new HashMap<String, AttributeValue>();

        itemValues.put("pk", new AttributeValue(getCustomerPK(customer.getUID())));
        itemValues.put("sk", new AttributeValue(getCustomerSK(customer.getUID())));
        itemValues.put("name", new AttributeValue(customer.getName()));
        if (customer.getAddresses().size() > 0 ) {
            itemValues.put("addresses", new AttributeValue(customer.getAddresses()));
        }

        try {
            this.ddb.putItem(this.tableName, itemValues);
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            throw new DatabaseClientException(e.getMessage());
        }
    }

    public Customer getCustomer(long uid) throws DatabaseClientException {

        HashMap<String, AttributeValue> searchKeys =
            new HashMap<String, AttributeValue>();

        searchKeys.put("pk", new AttributeValue(getCustomerPK(uid)));
        searchKeys.put("sk", new AttributeValue(getCustomerSK(uid)));

        GetItemRequest request = new GetItemRequest()
        .withKey(searchKeys)
        .withTableName(this.tableName);

        String name = "";
        ArrayList<String> addresses = null;

        try {
            Map<String, AttributeValue> returnedItems =
                ddb.getItem(request).getItem();

            if (returnedItems != null) {
                // Read in items
                name = returnedItems.get("name").getS();

                if (returnedItems.containsKey("addresses")) {
                    addresses = new ArrayList<String>(returnedItems.get("addresses").getSS());
                }

            } else {
                // No items found, not ideal
                System.out.println("No item found with the key! " + uid );
                return null;
            }

        } catch (AmazonServiceException e) {
            throw new DatabaseClientException(e.getErrorMessage());
        }

        if (addresses == null) {
            return new Customer(uid, name);
        }

        return new Customer(uid, name, addresses);

    }


    // Helper Functions

    private String getCustomerPK(long uid) {
        return "customer#" + String.valueOf(uid);
    }
    private String getCustomerSK(long uid) {
        return getCustomerPK(uid);
    }

    private long parseCustomerPK(String pk) {
        return Long.parseLong(stripKeyHeader(pk));
    }
    private long parseCustomerSK(String sk) {
        return parseCustomerPK(sk);
    }

    // Get the string after the # for dynamodb primary keys
    private String stripKeyHeader(String key) {
        return key.substring(key.indexOf("#"));
    }
}
