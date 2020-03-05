
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.*;
import java.io.*;


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
        String[] fileHeaders = {"mini","small, medium, large"};

        String[] tableInfo = {"customers", "products", "orders"};

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
                                    {"price", DECIMAL, 15, 2},
                                    {"description", VARCHAR, 1000},
                                    {"posting_date", DATE}
                                  };

        Object[][] orderInfo = {
                                    {"order_id", BIGINT}, 
                                    {"customer_id", BIGINT},
                                    {"product_id", BIGINT},
                                    {"quantity", INT, 10000},
                                    {"purchase_time", TIMESTAMP}
                                  };

        generateFile("testFile", 10, "customers", customerInfo, null, null);
    }



    // Assumes the first value will be the primary key.
    // Will return all the primary keys, helpful for constructing tables 
    // dependent on it   
    public static long[] generateFile(String fileName, int numberOfRows, 
                                      String tableName, Object[][] tableInfo, 
                                      long[] uidSet1, long[] uidSet2){

        // Number of records that are going to be written out together 
        int batchSize = 500;
        // Number of attributes in a record
        int numAttributes = tableInfo.length;

        // Write the file header

        // Csv header holds the first line of the csv file 
        String csvHeader = (String) tableInfo[0][0];

        for(int i = 1; i< tableInfo.length; i++){
            csvHeader+= ", "+ tableInfo[i][0];
        }

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

                        String outputStr="\"";

                        if (type == BIGINT){
                            // Going to generate using unique ids 
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
                            outputStr += key;
                        }
                        
                        else if (type == INT){
                            int maxValue = (int) tableInfo[i][2];
                            int randomValue = (int) (Math.random()*maxValue);
                            outputStr += randomValue; 
                        }
                        else if (type == VARCHAR){

                        }
                        else if (type == DECIMAL){

                        }
                        else if (type == DATE){

                        }
                        else if (type == TIMESTAMP){

                        }
                        outputStr += "\"";
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

                System.out.println(outputString);

                if(totalRecordsGenerated == numberOfRows){
                    break;
                }
            }
        } 
        catch (IOException x) {
            System.err.println(x);
        }


        return null;
    }

}

