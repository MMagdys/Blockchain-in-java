// The BlockChain class should maintain only limited block nodes to satisfy the functionality.
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    /**
     * create an empty blockchain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */

    public class Node{
        
        private UTXOPool uPool;
        private Block block;
        
        public Node(Block block, UTXOPool prevUTXOPool){
        	this.block = block;
            if (prevUTXOPool != null)
                this.uPool = new UTXOPool(prevUTXOPool);
            else
                this.uPool = new UTXOPool();
            this.updateUTXOPool();
        }

        private void updateUTXOPool(){
            ArrayList<Transaction> trans = this.block.getTransactions();
            UTXO newUTXO;
            for (Transaction tx : trans){
                ArrayList<Transaction.Output> outputs = tx.getOutputs();
                for (int i = 0; i < outputs.size(); i++){
                    System.out.println("Adding TX of hash: " + tx.getHash());
                    newUTXO = new UTXO(tx.getHash(), i);
                    this.uPool.addUTXO(newUTXO, outputs.get(i));   
                }
            }
        }

        public Block getBlock(){
            return this.block;
        }


        public UTXOPool getUTXOPool (){
            return this.uPool;
        }
        
    }


    TreeMap<Integer, ArrayList<Node>> theChain;
    TransactionPool transPool;
    UTXOPool uPool ;
    TxHandler handler;

    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
    	// List<Block> theChain = new ArrayList<Block>();
    	this.theChain = new TreeMap<>();
        this.uPool = new UTXOPool();
        this.handler = new TxHandler(uPool);
        this.transPool = new TransactionPool();
        
        Node genesisNode = new Node(genesisBlock, null);
        this.addGenesisBlock(genesisBlock);
        this.addNode(1, genesisNode);
        System.out.println("The Blockchain Nodes: " + this.theChain);
    }


    public void addNode(int height, Node node){
        ArrayList<Node> nodeList = this.theChain.get(height);
        UTXOPool prevUTXOPool = null;
        if(height > 1)
            prevUTXOPool = this.getPrevUTXOPool(height-1, node);
        if(nodeList != null){
            nodeList.add(node);
        }
        else{
        	nodeList = new ArrayList<Node>();
        	nodeList.add(node);
        }
        this.theChain.put(height, nodeList);
        this.getMaxHeightBlock();
    }


    public UTXOPool getPrevUTXOPool (int height, Node newNode){

        byte[] prevHash = newNode.getBlock().getPrevBlockHash();
        ArrayList<Node> nodeList = this.theChain.get(height);
        for (Node node : nodeList){
                if(node.getBlock().getHash() == prevHash){
                    return node.getUTXOPool();
                }
            }
        return null;
    }


    public int getBlockHeightFromHash (byte[] prevHash){
        int maxHeight = this.getMaxHeight();
        ArrayList<Node> nodesList;
        int limit = Math.max(maxHeight - CUT_OFF_AGE - 1, 0);
        // System.out.println("LIMIT: " + maxHeight + "\t" + limit);
        for(int i = maxHeight; i > limit; i--){
            nodesList = this.theChain.get(i);
            for (Node node : nodesList){
                if(node.getBlock().getHash() == prevHash){
                    return i;
                }
            }
        }
        return -1;
    }


    public void removeTransactionByNode(Node node){
        
        ArrayList<Transaction> trans = node.getBlock().getTransactions();
        for (Transaction tx : trans){
            this.transPool.removeTransaction(tx.getHash());     
        }
    }


    public Node getParentAtHeight(int height, Node latestNode){
        
        byte[] blockHash = latestNode.getBlock().getPrevBlockHash();
        ArrayList<Node> nodesList;
        for (int i = this.getMaxHeight()-1; i >= height; i-- ){
            // System.out.println(""+i + "\t"+ latestNode+"\t" + blockHash + "\n" + nodesList);
            nodesList = this.theChain.get(i);
            for (Node node : nodesList){
                if(node.getBlock().getHash() == blockHash){
                    blockHash = node.getBlock().getPrevBlockHash();
                    if (i == height)
                    	return node;
                }
            }
        }
        return null;
    }


    public void reOrganizeTheChain(Node latestNode){
        int maxHeight = this.getMaxHeight();
        ArrayList<Node> nodesList = this.theChain.get(maxHeight- CUT_OFF_AGE);
        if (nodesList.size() > 1){
            System.out.println("Needs Re-organization");
            Node parentNode = this.getParentAtHeight(maxHeight- CUT_OFF_AGE, latestNode);
            for (Node node : nodesList){
                if(node.getBlock().getHash() != parentNode.getBlock().getHash()){
                    System.out.println("removing: " + node);
                    removeTransactionByNode(node);
                }
            }
            nodesList = new ArrayList<Node>();
            nodesList.add(parentNode);
            this.theChain.put((maxHeight- CUT_OFF_AGE), nodesList);
            System.out.println("The Blockchain Nodes: " + this.theChain);
        }
    }

    
    public Node getLongestNode(){
    	ArrayList<Node> latesttNodes = this.theChain.get(this.theChain.lastKey());
        System.out.println("The lastest Nodes: " + latesttNodes);
        return latesttNodes.get(0);
    }


    public int getMaxHeight() {
        return this.theChain.lastKey();
    }
    
    
    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return this.getLongestNode().getBlock();
    }


    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
    	return this.getLongestNode().getUTXOPool();

    }


    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
    	return this.transPool;
    }



    /**
     * Add {@code block} to the blockchain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}, where maxHeight is 
     * the current height of the blockchain.
	 * <p>
	 * Assume the Genesis block is at height 1.
     * For example, you can try creating a new block over the genesis block (i.e. create a block at 
	 * height 2) if the current blockchain height is less than or equal to CUT_OFF_AGE + 1. As soon as
	 * the current blockchain height exceeds CUT_OFF_AGE + 1, you cannot create a new block at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
        int height = this.getBlockHeightFromHash(block.getPrevBlockHash());
        System.out.println("Adding Block at height: " + (height+1));
        if(height == -1)
            return false;

        Node newNode = new Node(block, null);
        this.addNode(height+1, newNode);
        // System.out.println("The Blockchain Nodes: " + this.theChain);
        // Re-Organize the Chain
        if (height+1 > CUT_OFF_AGE)
            this.reOrganizeTheChain(newNode);
    	for (Transaction tx : block.getTransactions()){
    		this.addTransaction(tx);
    	}
    	return true;
    }


    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        this.transPool.addTransaction(tx);
    }


    public void addGenesisBlock (Block block){
        // IMPLEMENT THIS
    	System.out.println("Adding COIN of hash: " + block.getCoinbase().getHash()+ block.getCoinbase().getOutputs());
    	if (block.getCoinbase() != null)
    		this.handler.handleCoin(block.getCoinbase());
    }
    
}