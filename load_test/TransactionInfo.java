public class TransactionInfo{


    // total time spent during transactions in an epoch
    private long[] transactionTimePerEpoch;
    // the total number of transactions completed in an epoch
    private long[] totalNumTransactionsPerEpoch;

    private long totalTransactionTime;

    private int totalNumTransactions;


    private long startTime;
    //Holds the number of epochs we will handle.
    //An epoch is by default 60 seconds 
    private int numEpochs;



    public TransactionInfo(){

    }

    public TransactionInfo(long startTime, int numEpochs){
        this.startTime = startTime;
        this.numEpochs = numEpochs; 

        transactionTimePerEpoch = new long[numEpochs];
        totalNumTransactionsPerEpoch = new long[numEpochs];
    }

    public void addTransaction(long txnStartTime, long txnEndTime){
        // Figure out the epoch 
        int epoch = (int) ((txnStartTime - startTime) / (1000 * 60) );
        System.out.println(epoch); 
        if (numEpochs <= epoch){
            System.err.println("ERROR: Epoch " + epoch + "for txn starting at " + 
                                    txnStartTime +"exceeds number of valid epochs");
            return;
        } 

        long timeInTransaction = txnEndTime - txnStartTime;

        totalNumTransactionsPerEpoch[epoch] += 1; 

        transactionTimePerEpoch[epoch] += timeInTransaction;

        totalTransactionTime += timeInTransaction;

        totalNumTransactions += 1;
    }

    public long getTotalNumTransactions(){
        return totalNumTransactions; 
    }

    public String toString(){

        String outputStr = "";
        outputStr += "Total Transactions: " + totalNumTransactions  + "\n";
        outputStr += "Total Time Of Transactions: " + totalTransactionTime + "\n";
        outputStr += "Transaction time per epoch: " + arrToString(transactionTimePerEpoch) + "\n";
        outputStr += "Total transactions per epoch: " + arrToString(totalNumTransactionsPerEpoch) + "\n";

        return outputStr; 
    }

    public static String arrToString(long[] arr){
        if (arr.length == 0){
            return "[]";
        }
        String outputStr = "[" + arr[0]; 
        
        for(int i = 1; i< arr.length; i++){
            outputStr += ", " + arr[i];

        }
        outputStr += "]";

        return outputStr;
    }
}
