
package ordersapp.dbclients;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.regions.Regions;
import com.amazonaws.regions.Region;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ordersapp.*;

// Client to interact with Dynamo to return objects that a 
// ordering system that takes orders from customers would use.
// This aims to be an acceptable single table design using ddb
public class DynamoClient implements DatabaseClient {

    protected String tableName;
    protected String indexName;
    protected AmazonDynamoDB ddb;

    public DynamoClient() {

    }

    public DynamoClient(String tableName, String indexName) {
        this.tableName = tableName;
        this.indexName = indexName;
        this.ddb = AmazonDynamoDBClient.builder()
                   .withRegion(Regions.US_EAST_1)
                   .build();
    }

    public DynamoClient(String tableName, String indexName, String region) {
        this.tableName = tableName;
        this.indexName = indexName;
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

    public void createOrder(Order order) throws DatabaseClientException {
        HashMap<String, AttributeValue> itemValues =
            new HashMap<String, AttributeValue>();

        itemValues.put("pk", new AttributeValue(getCustomerPK(order.getCustomerUID())));
        itemValues.put("sk", new AttributeValue(getOrderSK(order.getOrderUID())));
        itemValues.put("address", new AttributeValue(order.getAddress()));
        // Converts date to a long here
        itemValues.put("createDate", new AttributeValue().withN(String.valueOf(order.getCreateDate().getTime())));

        try {
            this.ddb.putItem(this.tableName, itemValues);
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            throw new DatabaseClientException(e.getMessage());
        }
    }

    public Order getOrder(long orderUID, long customerUID) throws DatabaseClientException {

        HashMap<String, AttributeValue> searchKeys =
            new HashMap<String, AttributeValue>();

        searchKeys.put("pk", new AttributeValue(getCustomerPK(customerUID)));
        searchKeys.put("sk", new AttributeValue(getOrderSK(orderUID)));

        GetItemRequest request = new GetItemRequest()
        .withKey(searchKeys)
        .withTableName(this.tableName);

        Order order = null;

        try {
            Map<String, AttributeValue> returnedItems =
                ddb.getItem(request).getItem();

            if (returnedItems != null) {

                order = new Order(
                    orderUID,
                    customerUID,
                    new Date(Long.parseLong(returnedItems.get("createDate").getN())),
                    returnedItems.get("address").getS());


            } else {
                // No items found, not ideal
                return null;
            }

        } catch (AmazonServiceException e) {
            throw new DatabaseClientException(e.getErrorMessage());
        }

        return order;
    }

    public void createItem(Item item) throws DatabaseClientException{
        HashMap<String, AttributeValue> itemValues =
            new HashMap<String, AttributeValue>();

        itemValues.put("pk", new AttributeValue(getOrderSK(item.getOrderUID())));
        itemValues.put("sk", new AttributeValue(getItemSK(item.getItemUID())));
        itemValues.put("quantity", new AttributeValue(String.valueOf(item.getQuantity())));
        itemValues.put("customerUID", new AttributeValue(String.valueOf(item.getCustomerUID())));
        // Converts date to a long here but stores as a number in ddb 
        itemValues.put("orderDate", new AttributeValue().withN(String.valueOf(item.getOrderDate().getTime())));
        try {
            this.ddb.putItem(this.tableName, itemValues);
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            throw new DatabaseClientException(e.getMessage());
        }
    }

    public ArrayList<Item> getItems(long orderUID) throws DatabaseClientException {
        return null;
    }

    public ArrayList<Item> getRecentItems(long customerUID, Date date) throws DatabaseClientException{

        QueryRequest query = new QueryRequest(this.tableName);
        query.setIndexName(this.indexName);
        query.setScanIndexForward(false); // scan index backwards to get most recent dates
        query.setLimit(5); // Only get ten most recent items
        query.withKeyConditionExpression("customerUID = :cUID");
        query = query.addExpressionAttributeValuesEntry(":cUID", new AttributeValue(String.valueOf(customerUID)));

        ArrayList<Item> outputItems = new ArrayList<Item>();

        try{
            QueryResult queryResult = this.ddb.query(query);

            List<Map<String,AttributeValue>> queryItems = queryResult.getItems();

            for(int i = 0; i < queryItems.size(); i ++){
                outputItems.add(parseItemResult(queryItems.get(i)));
            }

        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            throw new DatabaseClientException(e.getMessage());
        }


        return outputItems;
    }

    // Converts the attribute map that ddb returns into an item that we care about 
    protected Item parseItemResult(Map<String, AttributeValue> attributeMap){

        if (attributeMap == null){
            return null;
        }


        return new Item(
            Long.parseLong(stripKeyHeader(attributeMap.get("sk").getS())),
            Long.parseLong(stripKeyHeader(attributeMap.get("pk").getS())),
            Long.parseLong(attributeMap.get("customerUID").getS()),
            Integer.parseInt(attributeMap.get("quantity").getS()),
            new Date(Long.parseLong(attributeMap.get("orderDate").getN())));

    }

    // Helper Functions

    protected String getCustomerPK(long uid) {
        return "customer#" + String.valueOf(uid);
    }
    protected String getCustomerSK(long uid) {
        return getCustomerPK(uid);
    }

    protected long parseCustomerPK(String pk) {
        return Long.parseLong(stripKeyHeader(pk));
    }
    protected long parseCustomerSK(String sk) {
        return parseCustomerPK(sk);
    }

    protected String getOrderSK(long uid) {
        return "order#" + String.valueOf(uid);
    }

    protected String getItemSK(long uid){
        return "item#" + String.valueOf(uid);
    }

    // Get the string after the # for dynamodb primary keys
    protected String stripKeyHeader(String key) {
        return key.substring(key.indexOf("#")+1);
    }
}
