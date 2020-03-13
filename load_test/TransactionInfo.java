
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

import java.util.ArrayList;
import java.util.Collections;

public class TransactionInfo{


    // total time spent during transactions in an epoch
    private long[] transactionTimePerEpoch;
    // the total number of transactions completed in an epoch
    private long[] totalNumTransactionsPerEpoch;
    // Holds the transaction times, yes all of them 

    // Can tell whether transactions are sorted
    private boolean isSorted = false;
    // Holds how long each transaction took
    private ArrayList<Long> transactionTimes;

    private long totalTransactionTime;

    private int totalNumTransactions;

    private String tag; 

    private long startTime;
    //Holds the number of epochs we will handle.
    //An epoch is by default 60 seconds 
    private int numEpochs;

    // holds average transaction times
    // 50th percentile, 99th percentile, 99.9th percentile
    private double[] avgTxnTimes = new double[3];

    private double[] percentiles = {.5, .99, .999};

    public TransactionInfo(){

    }

    // Create TransactionInfo Object by reading the file defined by the path
    public TransactionInfo(String path, String allTransactionsPath){

        transactionTimes = new ArrayList<Long>();

        readFromJsonFile(path, allTransactionsPath);

    }

    public TransactionInfo(long startTime, int numEpochs){
        this.startTime = startTime;
        this.numEpochs = numEpochs; 

        transactionTimePerEpoch = new long[numEpochs];
        totalNumTransactionsPerEpoch = new long[numEpochs];
        //No tag by default
        tag = "";
        transactionTimes = new ArrayList<Long>();
    }

    public void addTransaction(long txnStartTime, long txnEndTime){
        // Figure out the epoch 
        int epoch = (int) ((txnStartTime - startTime) / (1000 * 60) );
        //System.out.println(epoch); 
        if (numEpochs <= epoch){
            System.err.println("ERROR: TransactionInfo: Epoch " + epoch + 
                                "for txn starting at " + 
                                    txnStartTime +"exceeds number of valid epochs");
            return;
        } 

        long timeInTransaction = txnEndTime - txnStartTime;

        transactionTimes.add(timeInTransaction);

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


    // Let's do some math 
    public void compute(){
        Collections.sort(transactionTimes);
        isSorted = true;

        // Let's calculate the tail latency

        int sizeTxnTimes = transactionTimes.size();

        for(int i = 0; i < percentiles.length; i++){

            long timeSum = 0;
            int numberOfTransactions = (int) (sizeTxnTimes * (1-percentiles[i]));
            int startingPosition = sizeTxnTimes - numberOfTransactions;

            for (int j = startingPosition; j < sizeTxnTimes; j++){
                timeSum += transactionTimes.get(j);
            }

            double avgTime = timeSum / numberOfTransactions;

            avgTxnTimes[i] = avgTime; 
        }
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

        // Let's combine

        if (!isSorted){
            Collections.sort(transactionTimes);
        } 

        if (!input.isSorted){
            Collections.sort(input.transactionTimes);
        }

        ArrayList<Long> newTransactionTime = new ArrayList<Long>();

        while (transactionTimes.size()>0 || input.transactionTimes.size() > 0){
            
            if(transactionTimes.size() == 0){
                newTransactionTime.add(input.transactionTimes.remove(0));
                continue; 
            }

            if(input.transactionTimes.size() == 0){
                newTransactionTime.add(transactionTimes.remove(0));
                continue; 
            }


            if(input.transactionTimes.get(0) < transactionTimes.get(0)){
                newTransactionTime.add(input.transactionTimes.remove(0));
            }
            else{
                newTransactionTime.add(transactionTimes.remove(0));
            }
        }

        transactionTimes = newTransactionTime;
        return true;
    }

    public void toJsonFile(String path, String allTransactionsPath){

        compute();

        // First let's convert this to json
        JSONObject rootObj = new JSONObject();
        rootObj.put("tag", tag);
        rootObj.put("totalNumTransactions", totalNumTransactions);
        rootObj.put("totalTransactionTime", totalTransactionTime);
        rootObj.put("numEpochs", numEpochs);

        // Add percentile info
        JSONObject percentileObj = new JSONObject();

        JSONArray percentileArray = new JSONArray();
        JSONArray timeArray = new JSONArray();

        for(int i = 0; i < avgTxnTimes.length; i++){
            percentileArray.add(percentiles[i]);
            timeArray.add(avgTxnTimes[i]);
        }

        percentileObj.put("percentiles", percentileArray);
        percentileObj.put("avgTxnTimes", timeArray);

        rootObj.put("percentileInfo", percentileObj);



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

        // Now let's write to the allTransactionsPath

        JSONObject obj = new JSONObject();

        JSONArray txnTimes = new JSONArray();

        for(int i = 0; i< transactionTimes.size(); i++){
            txnTimes.add(transactionTimes.get(i));
        }

        obj.put("transactionTimes", txnTimes);

        Path outputPath = Paths.get(allTransactionsPath);
        byte[] outputData = obj.toJSONString().getBytes();

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(outputPath))){
            out.write(outputData, 0, outputData.length);
        }
        catch (IOException x) {
            System.err.println(x);
        }

    }

    public boolean readFromJsonFile(String path, String allTransactionsPath){
        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(path)) {

            JSONObject jsonObject = (JSONObject) parser.parse(reader);


            // Deal with the arrays 
            JSONArray txnPerEpoch = (JSONArray) jsonObject.get("totalNumTransactionsPerEpoch");

            JSONArray txnTimePerEpoch = (JSONArray) jsonObject.get("transactionTimePerEpoch");

            totalNumTransactionsPerEpoch = copyToArray(txnPerEpoch);  
             
            transactionTimePerEpoch = copyToArray(txnTimePerEpoch);  

            // Read in the numbers 
           
            try{
                JSONObject percentileObj = (JSONObject) jsonObject.get("percentileInfo"); 

                JSONArray percentileArray = (JSONArray) percentileObj.get("percentiles");
                JSONArray timeArray = (JSONArray) percentileObj.get("avgTxnTimes");

                for(int i = 0; i < percentileArray.size(); i++){
                    percentiles[i] = (Double) percentileArray.get(i);
                    avgTxnTimes[i] = (Double) timeArray.get(i);
                }
            }
            catch(Exception e){
                System.err.println("ERROR: TransactionInfo: Error reading in percentile info");
                System.err.println(e.toString());
            }


            if (jsonObject.get("tag") != null){
                tag = (String) jsonObject.get("tag");
            }

            totalNumTransactions = ((Number) jsonObject.get("totalNumTransactions")).intValue();
            totalTransactionTime = (Long) (jsonObject.get("totalTransactionTime"));
            numEpochs = ((Number) jsonObject.get("numEpochs")).intValue();

            // Now read in from all transactions path

            Reader reader2 = new FileReader(allTransactionsPath);

            jsonObject = (JSONObject) parser.parse(reader2);

            JSONArray allTransactions = (JSONArray) jsonObject.get("transactionTimes");

            transactionTimes.clear();

            for(int i = 0; i< allTransactions.size(); i++){
                transactionTimes.add((Long) allTransactions.get(i));
            }

            Collections.sort(transactionTimes);
            isSorted = true;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        compute();


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
