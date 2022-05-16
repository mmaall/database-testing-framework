
package data_generator;

import java.lang.Math;

public class StringGenerator {


    //Padding it with spaces makes it look more like sentences
    private static final String validCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "0123456789" +
            "            ";

    //Generate a string of a given length

    // Lost characters are how many characters we are willing to lose. A zero means
    // That the string will be fully the length specified.
    public static String generateAlphaNumeric(int length, int lostCharacters) {

        String output = "";
        int numCharacters = validCharacters.length();

        if (lostCharacters >= length) {
            lostCharacters = 0;
        }

        length = length - (int) (Math.random() * lostCharacters);

        for (int i = 0; i < length; i++) {

            char newChar = validCharacters.charAt((int) (Math.random() * numCharacters));
            output += newChar;
        }
        return output;
    }

    // Generates a string that is going to be a decimal number
    public static String generateDecimal(int leftOfDecimal, int rightOfDecimal) {
        String output = "";

        for (int i = 0; i < leftOfDecimal; i++) {
            int digit =  (int) (Math.random() * 10);
            output += digit;
        }


        if (rightOfDecimal > 0) {
            output += ".";
        }

        for (int i = 0; i < rightOfDecimal; i++) {
            int digit = (int) (Math.random() * 10);
            output += digit;
        }

        return output;
    }
}