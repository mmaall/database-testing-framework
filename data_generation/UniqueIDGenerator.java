
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


// Static class that generates some unique identifiers
class UniqueIDGenerator{

    private String ipAddress; 

    public UniqueIDGenerator(){
        // Finds the ipAddress so we only have to do it once  
        try{
            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ipAddress = socket.getLocalAddress().getHostAddress();
        }
        catch(Exception e){
            System.out.printf("Unknown address\n");
            System.out.printf("%s\n", e.toString());
        }


    }

    public String getUID(){
        UID id = new UID();
        //Prepends IP addres to unique ID so they are always unique accross machines 
        return ipAddress+":"+id.toString();
    }

    public String getAddress(){
        return ipAddress;         
    } 

}