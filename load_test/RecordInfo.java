
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Arrays;


class RecordInfo{

    private String filePath; 

    private long[] customerUIDs;

    private long[] productUIDs;

    private long[] orderUIDs; 


    public RecordInfo(){

    }

    public RecordInfo(String path){
        filePath = path;

        // Let's try and read in the json

        readFromFile();

    }


    public long getCustomerUID(){

        int chosenOne = (int) (Math.random() * customerUIDs.length);
        return customerUIDs[chosenOne]; 
    }

    public long getProductUID(){
        int chosenOne = (int) (Math.random() * productUIDs.length);
        return productUIDs[chosenOne]; 
    }

    public long getOrderUID(){
        int chosenOne = (int) (Math.random() * orderUIDs.length);
        return orderUIDs[chosenOne]; 
    }


    static long[] copyToArray(JSONArray arr){
        long[] outputArr = new long[arr.size()];
        for(int i = 0; i<arr.size(); i++){
            outputArr[i] = (long) arr.get(i);
        }

        return outputArr;
    }

    public boolean readFromFile(){


        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(filePath)) {

            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            System.out.println(jsonObject);

            JSONArray customerObj = (JSONArray) jsonObject.get("customers");

            JSONArray productObj = (JSONArray) jsonObject.get("products");

            JSONArray orderObj = (JSONArray) jsonObject.get("orders");


            customerUIDs = copyToArray(customerObj);  
             
            productUIDs = copyToArray(productObj);  
            
            orderUIDs = copyToArray(orderObj);  


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return true;
    }

}
