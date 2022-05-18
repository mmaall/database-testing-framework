package data_generator;

public class DataGeneratorException extends Exception{

   private String details;

    public DataGeneratorException(String details) {
        this.details = details;
    }

    public String getDetails() {
        return this.details;
    } 
}