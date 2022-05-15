
package orm; 

// Exception to handle when invalid values are supplied 
public class DatabaseClientException extends Exception {
    private String details; 


    public DatabaseClientException(String details){
        this.details = details;
    }

    public String getDetails(){
        return this.details;
    }
}