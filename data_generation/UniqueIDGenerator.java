
// Class to generate unique identifiers

/**
 * WARNING
 * This is not secure in any way, shape, or form. 
 * Unique IDs can be guessed extremely easily. This is just used to generate 
 * unique identifiers for tests. Please do not use this for anything important.
**/

import java.rmi.server.UID;
import java.net.InetAddress; 
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.lang.Math;

// Static class that generates some unique identifiers
class UniqueIDGenerator{


    private static final int SIZE_OF_INT = 4;
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


    // Default constructur is going to use the ip address to derive the
    // system identifier

    //TODO: This doesn't work 
    public UniqueIDGenerator(){
        // Finds the ipAddress so we only have to do it once  
        try{
            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            systemIdentifier = (byte) socket.getLocalAddress().hashCode();
        }
        catch(Exception e){
            System.out.printf("Unknown address\n");
            System.out.printf("%s\n", e.toString());
        }

    }


    // Take in a systemIdentifier, this identifies the system we are on
    // Also takes in a process ID, this identifies which process that is being used 

    public UniqueIDGenerator(byte systemID, byte processID) throws InvalidValueException{
        // Finds the ipAddress so we only have to do it once  

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

    public int getUID(){
        UID id = new UID();
        int uidHash = id.hashCode();

        System.out.println("uidHash: "+ uidHash);
        int numReservedBits = processBits + systemBits;
        int totalBitsInt = SIZE_OF_INT * 8; 
        int topBitMask = (int) Math.pow(2, totalBitsInt - numReservedBits) - 1; 
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
        topBitMask = topBitMask << totalBitsInt - numReservedBits;

        // System.out.println("Mask application: "+topBitMask);
        
        // Add those first bits 
        uidHash = uidHash | topBitMask;
       
        return uidHash;
    }


}


//Exception to be thrown when invalid paramters are used 
class InvalidValueException extends Exception{

    public InvalidValueException(){
        super();
    }

    public InvalidValueException(String message){
        super(message);
    }


}