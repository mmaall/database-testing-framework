package data_generator;

import ordersapp.dbclients.DatabaseClient;
import ordersapp.dbclients.DatabaseClientException;
import ordersapp.*;
import data_generator.helpers.StringGenerator;

import java.util.UUID;
import java.lang.Math;
import java.util.Date;
import java.util.HashMap;

public class DataGenerator {

    private final int weightCount = 3;

    private DatabaseClient dbClient;
    private double[] recordProportions;
    private long[] startingUIDs; 
    private HashMap<Long, Order> orderMap;
    private int recordCount;

    public DataGenerator(DatabaseClient dbClient, int[] weights) throws DataGeneratorException {

        this.dbClient = dbClient;
        if (weights.length != weightCount){
            throw new DataGeneratorException("Weight array must be of length 3");
        }

        this.recordProportions = readWeights(weights);

        // TODO: By default generate some uuids to start with 

    }

    public DataGenerator(DatabaseClient dbClient, int[] weights, long[] startingUIDs) throws DataGeneratorException {

        this.dbClient = dbClient;

        if (weights.length != weightCount){
            throw new DataGeneratorException("Weight array must be of length 3");
        }

        if (startingUIDs.length != weightCount){
            throw new DataGeneratorException("startingUIDs array must be of length 3");
        }

        this.startingUIDs = startingUIDs;
        this.recordProportions = readWeights(weights);
    }

    // Do a ton of writes using our db engine of choice
    // Returns the number of records that were created 
    public int run(int rows){
        this.recordCount = rows;

        int recordsCreated = 0;

        int totalCustomers = (int) (rows * recordProportions[0]);
        int totalOrders = (int) (rows * recordProportions[1]);
        int totalItems = (int) (rows * recordProportions[2]);
        recordsCreated += putCustomers(totalCustomers);
        recordsCreated += putOrders(totalOrders, (int) totalCustomers/2);
        recordsCreated += putItems(totalItems, totalOrders/2);

        return recordsCreated;

    }

    public long[] getStartingUIDs(){
        return this.startingUIDs;
    }

    public int[] getRecordsCreatedCount(){



        int[] output = {
            (int) (recordCount * recordProportions[0]),
            (int) (recordCount * recordProportions[1]),
            (int) (recordCount * recordProportions[2])
        };

        return output;
    }


    // Read in the weights and calculate the proportion of records
    private double[] readWeights(int[] weights){

        double[] outputArr = new double[weights.length];

        int sum = 0;

        for(int i = 0; i < weights.length; i++) {
            sum += weights[i];
        }

        for(int i =0; i< weights.length; i++){
            outputArr[i] = (double) weights[i]/sum;
        }

        return outputArr;

    }

    // Put a bunch of customers
    private int putCustomers(int count){

        int createdCustomerCount = 0;

        long startingUID = startingUIDs[0];

        for(int i = 0; i < count; i ++){
            Customer newCustomer = new Customer(
                startingUID + i, 
                StringGenerator.generateAlphaNumeric(15, 4));

            newCustomer.addAddress(StringGenerator.generateAlphaNumeric(40, 10));

            try{
                this.dbClient.createCustomer(newCustomer);
            } catch (DatabaseClientException e) {
                System.err.println(e.getDetails());
                continue;
            }
            createdCustomerCount += 1;

        }

        return createdCustomerCount;
    }

    // Create a bunch of orders
    // Customer count is the number of customers who the orders will be
    // distributed to. This is currently an even distribution. 
    // A lower customer count means customers will have more orders.
    private int putOrders(int orderCount, int customerCount){
        int createdOrders = 0; 
        long startingOrderUID = this.startingUIDs[1];
        long startingCustomerUID = this.startingUIDs[0];

        this.orderMap = new HashMap<Long, Order>();

        for(int i = 0; i < orderCount; i++){
            // Create a new customer

            long newOrderUID = startingOrderUID + i;

            // Get the customer ID based on our set of starting UIDs

            Order newOrder = new Order(
                    newOrderUID,
                    startingCustomerUID + (int) (Math.random() * customerCount),
                    new Date(),
                    StringGenerator.generateAlphaNumeric(40, 10)
                );

            try{
                this.dbClient.createOrder(newOrder);
            } catch (DatabaseClientException e) {
                System.err.println(e.getDetails());
                continue;
            }

            this.orderMap.put(newOrderUID, newOrder);

            createdOrders += 1;
        }

        return createdOrders;
    }

    // Similar to the above
    // Make itemCount numbers of items and then distribute them across
    // a set of orders 
    // TODO: Should this maintain stronger referential integrity? 
    private int putItems(int itemCount, int orderCount){

        int createdItems = 0; 
        long startingOrderUID = this.startingUIDs[1];
        long startingItemUID = this.startingUIDs[2];

        for(int i = 0; i < itemCount; i++){
 
            long newItemUID = startingItemUID + i;
            long orderUID = startingOrderUID + (int) (Math.random() * orderCount);
            int itemQuantity = ((int) Math.random() * 10) + 1;


            Order assosciatedOrder = this.orderMap.get(orderUID);

            Item newItem = new Item(
                newItemUID,
                orderUID,
                assosciatedOrder.getCustomerUID(),
                itemQuantity,
                assosciatedOrder.getCreateDate());

            try{
                this.dbClient.createItem(newItem);
            } catch (DatabaseClientException e) {
                System.err.println(e.getDetails());
                continue;
            }

            createdItems += 1;
        }

        return createdItems;
    }


}