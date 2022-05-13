
package data_generator; 

// Exception to handle when invalid values are supplied 
public class InvalidValueException extends Exception {
    private String details; 

    public InvalidValueException(String details){
        this.details = details;
    }

    public String getDetails(){
        return this.details;
    }
}