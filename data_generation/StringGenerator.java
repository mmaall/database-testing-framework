
import java.lang.Math;

class StringGenerator{


    //Padding it with spaces makes it look more like sentences
    private static final String validCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"+
                                           "abcdefghijklmnopqrstuvwxyz"+
                                           "0123456789"+
                                           "            ";

    //Generate a string of a given length 
    public static String generateString(int length){

        String output = "";
        int numCharacters = validCharacters.length();

        for (int i =0; i< length; i++){

            char newChar = validCharacters.charAt((int) (Math.random()*numCharacters));
            output+= newChar;
        } 
        return output;
    }
}