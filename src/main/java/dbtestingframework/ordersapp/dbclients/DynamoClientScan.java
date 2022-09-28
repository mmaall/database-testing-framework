package ordersapp.dbclients;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.regions.Regions;
import com.amazonaws.regions.Region;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ordersapp.*;

public class DynamoClientScan extends DynamoClient{


    public DynamoClientScan(String tableName, String indexName, String region) {
        this.tableName = tableName;
        this.indexName = indexName;
        this.ddb = AmazonDynamoDBClient.builder()
                   .withRegion(Regions.fromName(region))
                   .build();
    }

    public ArrayList<Item> getRecentItems(long customerUID, Date date) throws DatabaseClientException{

        ScanRequest scanRequest = new ScanRequest(super.tableName);
        scanRequest.withFilterExpression("customerUID = :cUID");
        scanRequest = scanRequest.addExpressionAttributeValuesEntry(":cUID", new AttributeValue(String.valueOf(customerUID)));

        ArrayList<Item> outputItems = new ArrayList<Item>();

        try{
            ScanResult scanResult = super.ddb.scan(scanRequest);

            List<Map<String,AttributeValue>> queryItems = scanResult.getItems();

            for(int i = 0; i < queryItems.size(); i ++){
                // Do nothing we're lazy and I do not want to write
                // the code to unmarshal this. Counting is enough
            }

        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            throw new DatabaseClientException(e.getMessage());
        }


        return new ArrayList<Item>();
    }

}

