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
        double outSum = 0;
        double inSum = 0;
        Transaction.Output prevTx;
//        System.out.println("Validation of TX: " + input.prevTxHash);

        for (Transaction.Input input : tx.getInputs()){
            tmpUTXO = new UTXO(input.prevTxHash, input.outputIndex);
            if(tmpUtxoPool.contains(tmpUTXO)){
                prevTx = tmpUtxoPool.getTxOutput(tmpUTXO);
//                System.out.println("yes", prevTrans.address);
                if(verifySignature(prevTx.address, tx.getRawTx(), input.signature)){
                    inSum += prevTx.value;
                    tmpUtxoPool.removeUTXO(tmpUTXO);
                }
                else{
                    return false;
                }
                
            }
            else{
                System.out.println("no");
                return false;
            }
        }
        
        for (Transaction.Output output : tx.getOutputs()){
            if(output.value >= 0)
                outSum += output.value;
            else
                return false;
        }

        if (inSum >= outSum)
            return true;

        return false;    
    }
    
    public UTXOPool isValidTx(Transaction tx, UTXOPool tmpUtxoPool) {
        // IMPLEMENT THIS

        UTXO tmpUTXO;
        double outSum = 0;
        double inSum = 0;
        Transaction.Output prevTx;

        for (Transaction.Input input : tx.getInputs()){
            tmpUTXO = new UTXO(input.prevTxHash, input.outputIndex);
            if(tmpUtxoPool.contains(tmpUTXO)){
                prevTx = tmpUtxoPool.getTxOutput(tmpUTXO);
                if(verifySignature(prevTx.address, tx.getRawTx(), input.signature)){
                    inSum += prevTx.value;
                    tmpUtxoPool.removeUTXO(tmpUTXO);
                }
                else{
                    return null;
                }
                inSum += tmpUtxoPool.getTxOutput(tmpUTXO).value;
                tmpUtxoPool.removeUTXO(tmpUTXO);
            }
            else{
                System.out.println("no");
                return null;
            }
        }
        
        for (Transaction.Output output : tx.getOutputs()){
            if(output.value >= 0)
                outSum += output.value;
            else
                return null;
        }

        if (inSum >= outSum)
            return tmpUtxoPool;

        return null;
    }


    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        ArrayList<Transaction> validTransactions = new ArrayList<Transaction>();
        UTXOPool tmpUtxoPool = new UTXOPool(this.utxoPool);
        UTXO tmpUTXO;

        for (Transaction tx : possibleTxs){
                ArrayList<Transaction.Output> outputs = tx.getOutputs();
                for (int i = 0; i < outputs.size(); i++){
                    System.out.println("Adding TX of hash: " + tx.getHash());
                    tmpUTXO = new UTXO(tx.getHash(), i);
                    tmpUtxoPool.addUTXO(tmpUTXO, outputs.get(i));   
                }
        }

        for (Transaction tx : possibleTxs){
            UTXOPool newPool = this.isValidTx(tx, tmpUtxoPool);
            if (newPool == null){
                System.out.println("NOT valid");
            }
            else{
                tmpUtxoPool = newPool;
                validTransactions.add(tx);
            }
        }
    	return validTransactions.toArray(new Transaction[0]);
    }
    
    
    public boolean handleCoin(Transaction tx) {

        System.out.println("Adding COIN of hash: " + tx.getHash());
        UTXO tmpUTXO = new UTXO(tx.getHash(), 0);
        this.utxoPool.addUTXO(tmpUTXO, tx.getOutput(0));
        return true;
    }

}
