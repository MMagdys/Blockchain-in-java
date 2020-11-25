import java.util.ArrayList;
import java.util.Arrays;


public class TxHandler {
	
	    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. 
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS

        UTXOPool tmpUtxoPool = new UTXOPool(this.utxoPool);
        UTXO tmpUTXO;
        for (Transaction.Input input : tx.getInputs()){
            tmpUTXO = new UTXO(input.prevTxHash, input.outputIndex);
            if(tmpUtxoPool.contains(tmpUTXO)){
                System.out.println("yes");
            }
            else{
                System.out.println("no");
            }
        //     Transaction prevTrans = getTransaction(input.prevTxHash).outputs[outputIndex];
        //    if( prevTrans ){
        //        verifySignature(prevTrans.address, prevTrans.getRawDataToSign(), input.signature)

        //    }
        //    else{
        //        return false;
        //    }
        	System.out.println(Arrays.toString(input.prevTxHash));
        	System.out.println((input.prevTxHash));

        	
            

        }
        return true;

        // for (Output output : tx.outputs){
        //     if(this.utxoPool.contains(output))

        // }
        
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        UTXO tmpUTXO;
        for (Transaction tx : possibleTxs){
            // if (this.isValidTx(tx)){
            //     System.out.println("valid");
            // }
            for (int i = 0; i < tx.getOutputs().size(); i++){
                System.out.println("Adding TX of hash: " + tx.getHash());
//                tmpUTXO = new UTXO(tx.getHash(), i);
            }
            
            
        }
    	return null;
    }
    
    
    public boolean handleCoin(Transaction tx) {

        System.out.println("Adding COIN of hash: " + tx.getHash());
        UTXO tmpUTXO = new UTXO(tx.getHash(), 0);
        this.utxoPool.addUTXO(tmpUTXO, tx.getOutput(0));
        return true;
    }

}
