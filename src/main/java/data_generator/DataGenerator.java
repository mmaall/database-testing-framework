
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import data_generation.UniqueIDGenerator;

class DataGenerator{

    public static final int BIGINT = 0;
    public static final int INT = 1;
    public static final int VARCHAR = 2;
    public static final int DECIMAL = 3;
    public static final int DATE = 4;
    public static final int TIMESTAMP = 5;




    public static void main(String[] args){


        // Holds the number of rows in each test
        int[] rowsPerTest = {100,10600000, 18000000, 35000000};
        // Holds all the headers of the files  
        String[] fileHeaders = {"mini","small" , "medium", "large"};

        String[] tableInfo = {"Customers", "Products", "Orders"};

        Object[][] customerInfo = {
                                    {"customer_id", BIGINT}, 
                                    {"name", VARCHAR, 50},
                                    {"address", VARCHAR, 125},
                                    {"age", INT, 100}
                                  };

        Object[][] productInfo = {
                                    {"product_id", BIGINT}, 
                                    {"name", VARCHAR, 50},
                                    {"category", VARCHAR, 125},
                                    {"price", DECIMAL, 4, 2},
                                    {"description", VARCHAR, 1000},
                                    {"quantity", INT, 500000},
                                    {"posting_date", DATE}
                                  };

        Object[][] orderInfo = {
                                    {"order_id", BIGINT}, 
                                    {"customer_id", BIGINT},
                                    {"product_id", BIGINT},
                                    {"quantity", INT, 100},
                                    {"purchase_time", TIMESTAMP}
                                  };

        Object[][] test = {
                                    {"order_id", BIGINT}, 
                                    {"customer_id", BIGINT},
                                    {"product_id", BIGINT},
                                    {"quantity", INT, 10000},
                                    {"purchase_time", TIMESTAMP},
                                    {"testThing", DATE}
                                };


        JSONObject outputObject = new JSONObject();


        JSONArray selectedCustomers = new JSONArray();

        JSONArray selectedProducts = new JSONArray();

        JSONArray selectedOrders = new JSONArray();

        for(int i = 0; i< 3; i++){
            String fileType = fileHeaders[i];
            int numRows = rowsPerTest[i];

            System.out.println("**** Generating a total of " + numRows + " rows");

            ArrayList<Long> customerUIDs = generateFile(fileType+"Customers.csv", (int) (numRows*.40), customerInfo, null, null, selectedCustomers);
            System.out.println("Customers generated");
            ArrayList<Long> productUIDs = generateFile(fileType+"Products.csv", (int)  (numRows*.20), productInfo, null, null, selectedProducts);
            System.out.println("Products generated");
            generateFile(fileType+"Orders.csv", (int) (numRows*.40), orderInfo, customerUIDs, productUIDs, selectedOrders); 
            System.out.println("Orders generated");
            


            outputObject.put("customers", selectedCustomers);
            //System.out.println("Total customerUID length: " + customerUIDs.size());
            //System.out.println("Selected Customers: " + selectedCustomers.size());
            outputObject.put("products", selectedProducts);
            outputObject.put("orders", selectedOrders);

            Path p = Paths.get("./"+fileType+"SelectedIDs.json");
            byte[] data = outputObject.toJSONString().getBytes();

            // Write out the data 
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(p))){
                out.write(data, 0, data.length);
            }
            catch (IOException x) {
                System.err.println(x);
            }
        }

        // Write out the jsonfile

        outputObject.put("customers", selectedCustomers);
        outputObject.put("products", selectedProducts);
        outputObject.put("orders", selectedOrders);
        System.out.println(selectedOrders);
    }



    // Assumes the first value will be the primary key.
    // Will return all the primary keys, helpful for constructing tables 
    // dependent on it   
    public static ArrayList<Long> generateFile(String fileName, int numberOfRows, 
                                      Object[][] tableInfo, 
                                      ArrayList<Long> uidSet1, ArrayList<Long> uidSet2,
                                      JSONArray selectedUIDs){
        System.out.println("Generating " + fileName);
        System.out.println("Rows: " + numberOfRows);
        // Number of records that are going to be written out together 
        int batchSize = 500;
        // Number of attributes in a record
        int numAttributes = tableInfo.length;

        ArrayList<Long> uniqueIDs = new ArrayList<Long>();

        int uidsCollected = 0;
        int uidsToCollect = (int) (numberOfRows * .05); 

        // Write the file header

        // Csv header holds the first line of the csv file 
        String csvHeader = (String) tableInfo[0][0];

        for(int i = 1; i< tableInfo.length; i++){
            csvHeader+= ", "+ tableInfo[i][0];
        }

        csvHeader +="\n";

        byte data[] = csvHeader.getBytes();
        Path p = Paths.get("./"+fileName);


        // Write out the data 
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(p))){
            out.write(data, 0, data.length);
        

            int totalRecordsGenerated= 0; 

            // Start looping through, write in batches 


            while(true){
                //Creates an array to hold all the data we will generate
                String generatedData[][] = new String[numAttributes][batchSize];

                String outputString = "";

                for(int i = 0; i< numAttributes; i++){
                    // get the type of the table
                    int type = (int) tableInfo[i][1];

                    for(int j = 0; j < batchSize; j++){

                        String outputStr="";

                        if (type == BIGINT){
                            // Going to generate using unique ids 
                            if (i == 0){
                            
                                UniqueIDGenerator generator;

                                try{
                                    UniqueIDGenerator tempGenerator = 
                                                            new UniqueIDGenerator();
                                    generator = tempGenerator;
                                }
                                catch(Exception e){
                                    System.out.println(e.toString());
                                    return null;
                                }
                                long key = generator.getUID();

                                if(uniqueIDs.size() < numberOfRows){
                                    uniqueIDs.add(key);
                                }

                                outputStr += key;

                                if (uidsCollected < uidsToCollect){
                                    selectedUIDs.add(key);
                                    uidsCollected++;
                                }

                            }
                            // Otherwise we will pick a key using a gausien distribution 
                            else{
                                ArrayList<Long> ids;
                                if (i == 1){
                                    ids = uidSet1;
                                }
                                else{
                                    ids = uidSet2;
                                }
                                Random rand = new Random();
                                int stdDev = ids.size()/4;
                                int mean = ids.size()/2;

                                int keyToPick = (int) (rand.nextGaussian()*stdDev + mean );

                                if (keyToPick < 0 || keyToPick >= ids.size()){
                                    keyToPick = (int)  (Math.random()*ids.size()); 
                                }

                                //System.out.println(keyToPick);
                                //System.out.println("ID Size: " + ids.size());

                                outputStr += ids.get(keyToPick);

                            }
                        }

                        else if (type == INT){
                            int maxValue = (int) tableInfo[i][2];
                            int randomValue = (int) (Math.random()*maxValue);
                            outputStr += randomValue; 
                        }
                        else if (type == VARCHAR){
                            outputStr += "\"";
                            int numCharacters = (int) tableInfo[i][2];

                            //Varachars have a variance of 30 characters. 
                            outputStr += StringGenerator.generateAlphaNumeric(numCharacters, 30);
                            outputStr += "\""; 
                        }
                        else if (type == DECIMAL){
                            int leftOfDecimal = (int) tableInfo[i][2];
                            int rightOfDecimal = (int) tableInfo[i][3];

                            outputStr += StringGenerator.generateDecimal(leftOfDecimal, rightOfDecimal); 
                        }
                        else if (type == DATE){
                            outputStr += createRandomDate(2010,2018).toString();


                        }
                        else if (type == TIMESTAMP){
                            outputStr += createRandomDate(2010, 2019).toString();

                            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                            String timeStr = timestamp.toString();

                            for(int n = 0; n<timeStr.length(); n++){
                                if(timeStr.charAt(n)== ' '){
                                    timeStr = timeStr.substring(n+1);
                                    break;
                                }
                            }
                            outputStr += " " +timeStr;
                        }
                        generatedData[i][j]= outputStr;

                    }

                }

                //generate the strings
                for(int i = 0; i< batchSize; i++){
                    if (totalRecordsGenerated ==numberOfRows){
                        break;
                    }
                    outputString += generatedData[0][i];
                    for(int j = 1; j < numAttributes; j++){

                        outputString += ", " + generatedData[j][i];
                    }
                    outputString += "\n";

                    totalRecordsGenerated++;

                }


                byte[] outputBytes = outputString.getBytes();

                out.write(outputBytes, 0, outputBytes.length);

                if (totalRecordsGenerated % 100000 == 0){
                    System.out.println("Total Records Generated: "+totalRecordsGenerated);
                }
                if(totalRecordsGenerated == numberOfRows){
                    out.close();
                    break;
                }
            }
        } 
        catch (IOException x) {
            System.err.println(x);
        }


        return  uniqueIDs;
    }


    /**
     * Borrowed this from the internet
     * Wasn't in the mood to write it myself. Thanks internet, you do great. 
     * src: https://www.logicbig.com/how-to/code-snippets/jcode-java-random-random-dates.html
     *
    **/

    public static int createRandomIntBetween(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }

    public static LocalDate createRandomDate(int startYear, int endYear) {
        int day = createRandomIntBetween(1, 28);
        int month = createRandomIntBetween(1, 12);
        int year = createRandomIntBetween(startYear, endYear);
        return LocalDate.of(year, month, day);
    }

}

