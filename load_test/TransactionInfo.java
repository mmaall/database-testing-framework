
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.*;
import java.io.*;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Arrays;


public class TransactionInfo{


    // total time spent during transactions in an epoch
    private long[] transactionTimePerEpoch;
    // the total number of transactions completed in an epoch
    private long[] totalNumTransactionsPerEpoch;

    private long totalTransactionTime;

    private int totalNumTransactions;

    private String tag; 

    private long startTime;
    //Holds the number of epochs we will handle.
    //An epoch is by default 60 seconds 
    private int numEpochs;



    public TransactionInfo(){

    }

    // Create TransactionInfo Object by reading the file defined by the path
    public TransactionInfo(String path){

        readFromJsonFile(path);

    }

    public TransactionInfo(long startTime, int numEpochs){
        this.startTime = startTime;
        this.numEpochs = numEpochs; 

        transactionTimePerEpoch = new long[numEpochs];
        totalNumTransactionsPerEpoch = new long[numEpochs];
        //No tag by default
        tag = "";
    }

    public void addTransaction(long txnStartTime, long txnEndTime){
        // Figure out the epoch 
        int epoch = (int) ((txnStartTime - startTime) / (1000 * 60) );
        //System.out.println(epoch); 
        if (numEpochs <= epoch){
            System.err.println("ERROR: Epoch " + epoch + "for txn starting at " + 
                                    txnStartTime +"exceeds number of valid epochs");
            return;
        } 

        long timeInTransaction = txnEndTime - txnStartTime;

        totalNumTransactionsPerEpoch[epoch] += 1; 

        transactionTimePerEpoch[epoch] += timeInTransaction;

        totalTransactionTime += timeInTransaction;

        totalNumTransactions += 1;
    }

    public long getTotalTransactionTime(){
        return totalTransactionTime;
    }

    public long getTotalNumTransactions(){
        return totalNumTransactions; 
    }

    public long[] getNumTransactionsPerEpoch(){
        return totalNumTransactionsPerEpoch;
    }

    public long[] getTransactionTimePerEpoch(){
        return transactionTimePerEpoch;
    }

    public int getNumEpochs(){
        return numEpochs;
    }

    public void setTag(String tag){
        this.tag = tag; 
    }

    // Moves all the information from input into this object. 
    public boolean combine(TransactionInfo input){


        if(numEpochs != input.numEpochs){
            return false;
        }
        

        totalNumTransactions += input.totalNumTransactions;
        totalTransactionTime += input.totalTransactionTime; 

        for(int i = 0; i < numEpochs; i++){
           totalNumTransactionsPerEpoch[i] += input.totalNumTransactionsPerEpoch[i];
           transactionTimePerEpoch[i] += input.transactionTimePerEpoch[i]; 

        }


        return true;
    }

    public void toJsonFile(String path){


        // First let's convert this to json
        JSONObject rootObj = new JSONObject();
        rootObj.put("tag", tag);
        rootObj.put("totalNumTransactions", totalNumTransactions);
        rootObj.put("totalTransactionTime", totalTransactionTime);
        rootObj.put("numEpochs", numEpochs);

        JSONArray txnPerEpoch = new JSONArray();
        JSONArray txnTimePerEpoch = new JSONArray();

        for(int i = 0; i < numEpochs; i++){
            txnPerEpoch.add(totalNumTransactionsPerEpoch[i]);
            txnTimePerEpoch.add(transactionTimePerEpoch[i]);
        }

        rootObj.put("totalNumTransactionsPerEpoch", txnPerEpoch);
        rootObj.put("transactionTimePerEpoch", txnTimePerEpoch);

        Path p = Paths.get(path);
        byte[] data = rootObj.toJSONString().getBytes();

        // Write out the data 
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(p))){
            out.write(data, 0, data.length);
        }
        catch (IOException x) {
            System.err.println(x);
        }
    }

    public boolean readFromJsonFile(String path){
        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(path)) {

            JSONObject jsonObject = (JSONObject) parser.parse(reader);


            // Deal with the arrays 
            JSONArray txnPerEpoch = (JSONArray) jsonObject.get("totalNumTransactionsPerEpoch");

            JSONArray txnTimePerEpoch = (JSONArray) jsonObject.get("transactionTimePerEpoch");

            totalNumTransactionsPerEpoch = copyToArray(txnPerEpoch);  
             
            transactionTimePerEpoch = copyToArray(txnTimePerEpoch);  

            // Read in the numbers 
            

            if (jsonObject.get("tag") != null){
                tag = (String) jsonObject.get("tag");
            }

            totalNumTransactions = ((Number) jsonObject.get("totalNumTransactions")).intValue();
            totalTransactionTime = (Long) (jsonObject.get("totalTransactionTime"));
            numEpochs = ((Number) jsonObject.get("numEpochs")).intValue();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return true;
    } 

    private static long[] copyToArray(JSONArray arr){
        long[] outputArr = new long[arr.size()];
        for(int i = 0; i<arr.size(); i++){
            outputArr[i] = (long) arr.get(i);
        }

        return outputArr;
    }


    public String toString(){

        String outputStr = "";
        outputStr += "Total Transactions: " + totalNumTransactions  + "\n";
        outputStr += "Total Time Of Transactions: " + totalTransactionTime + "\n";
        outputStr += "Transaction time per epoch: " + arrToString(transactionTimePerEpoch) + "\n";
        outputStr += "Total transactions per epoch: " + arrToString(totalNumTransactionsPerEpoch) + "\n";
        outputStr += "Number of epochs: " + numEpochs + "\n";

        return outputStr; 
    }

    public static String arrToString(long[] arr){
        if (arr.length == 0){
            return "[]";
        }
        String outputStr = "[" + arr[0]; 
        
        for(int i = 1; i< arr.length; i++){
            outputStr += ", " + arr[i];

        }
        outputStr += "]";

        return outputStr;
    }
}
