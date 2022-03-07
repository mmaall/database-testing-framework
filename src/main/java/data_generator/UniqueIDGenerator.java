
package data_generator; 

// Class to generate unique identifiers

/**
 * WARNING
 * This is not secure in any way, shape, or form. 
 * Unique IDs can be guessed extremely easily. This is just used to generate 
 * unique identifiers for tests. Please do not use this for anything important.
**/

import java.lang.Math;
import java.util.UUID;


// Static class that generates some unique identifiers
public class UniqueIDGenerator{


    private static final int SIZE_OF_INT = 4;
    private static final int SIZE_OF_LONG = 8;
    private static final int SIZE_OF_BYTE = 1;

    // Holds the unique system identifier
    private int systemIdentifier;
    //Holds the unique process identifier
    private int processIdentifier;
  
    // bits to uniquely identify the process 
    private final int processBits = 2;
    // bits to uniquely identify the system 
    private final int systemBits = 3;


    //Maximum value the process ID can hold
    // Currently reserving 2 bits for the process ID
    private int maxProcessID = (int) Math.pow(2, processBits);

    //Maximum value the systemID can hold
    //Currently reserving 3 bits for the system ID
    private int maxSystemID = (int) Math.pow(2, systemBits); 


    // Default constructor
    // Uses a process ID of 0 and system identifier of 0  
    public UniqueIDGenerator(){
        // Finds the ipAddress so we only have to do it once  
        systemIdentifier = 0;
        processIdentifier = 0;

    }


    // Take in a systemIdentifier, this identifies the system we are on
    // Also takes in a process ID, this identifies which process that is being used 

    public UniqueIDGenerator(byte systemID, byte processID) throws InvalidValueException{

        boolean invalidID =false;

        if (systemID <= maxSystemID){
            systemIdentifier = systemID; 
        }
        else{
            invalidID = true;
        }
        
        if (processID <= maxProcessID){
            processIdentifier = processID;
        }

        else{
            invalidID = true;
        }

        if(invalidID){
            //One the identifiers failed 

            String str = "UniqueIDGenerator error. systemID (%d) "+
                         "or processID(%d) exceeds maximum "+
                         "ID values (%d, %d).";

            //Let's throw that exception!!!!
            throw new InvalidValueException(str); 
        }

    }

    public long getUID(){

        UUID id = UUID.randomUUID();
        // uid is going to be the least significant bits of the uid
        long uidHash = id.getLeastSignificantBits();
        String uidString = id.toString();
        //System.out.println("uidHash: "+ uidHash);
        //System.out.println("Uid string: " + uidString);
        int numReservedBits = processBits + systemBits;
        int totalBitsLong = SIZE_OF_LONG * 8; 
        long topBitMask = (long) Math.pow(2, totalBitsLong - numReservedBits) - 1; 
        //System.out.println("Top Bit Mask: " + topBitMask);
        
        // Mask out the top bits to 0s 
        uidHash = uidHash & topBitMask; 

        topBitMask = 0;

        // add system bits
        topBitMask = topBitMask | systemIdentifier;
 
        // shift left to make room for processBits 
        topBitMask = topBitMask << processBits;

        // Add the process identifier 
        topBitMask = topBitMask | processIdentifier;

        //shift to apply to top bits
        topBitMask = topBitMask << totalBitsLong - numReservedBits;

        // System.out.println("Mask application: "+topBitMask);
        
        // Add those first bits 
        uidHash = uidHash | topBitMask;
       
        return uidHash;
    }


}

