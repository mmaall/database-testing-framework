
import load_test.*; 

public class DataAggregator{


    // Arguments [dataSize, numberOfMachines]

    public static void main(String[] args){

        String dataSize = args[0];
        int numberOfMachines = Integer.parseInt(args[1]);

        TransactionInfo[] txnInfoArray = new TransactionInfo[numberOfMachines];


        for(int i = 0; i < numberOfMachines; i++){

            String partFileName = i+"_"+dataSize+"Info";
            TransactionInfo tempInfo = new TransactionInfo(partFileName+"1.json", 
                                                            partFileName+"2.json");
        }

        TransactionInfo finalObject = aggregate(txnInfoArray);

        String fileMiddle = "FinalAggregate";

        finalObject.toJsonFile(dataSize+fileMiddle+ "1.json", dataSize+fileMiddle+"2.json");

    }

    public static TransactionInfo aggregate(TransactionInfo[] arr){
        TransactionInfo head = arr[0];

        for(int i = 1; i < arr.length; i++){
            head.combine(arr[i]);
        }

        return head;
    }

}
