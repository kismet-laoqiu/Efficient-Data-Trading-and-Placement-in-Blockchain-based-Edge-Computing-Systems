package com.example.demo.blockchain.block;

import com.example.demo.blockchain.podt.PoDTResult;
import com.example.demo.blockchain.podt.PoDT;
import com.example.demo.blockchain.store.RocksDBUtils;
import com.example.demo.blockchain.store.RocksDBforContent;
import com.example.demo.blockchain.store.RocksDBforWallet;
import com.example.demo.blockchain.transaction.Content;
import com.example.demo.blockchain.transaction.MerkleTree;
import com.example.demo.blockchain.transaction.Transaction;
import com.example.demo.blockchain.transaction.UTXOSet;
import com.example.demo.blockchain.util.ByteUtils;
import com.example.demo.blockchain.wallet.WalletDB;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 区块
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Block implements Serializable {


    /**
     * 发块地址
     */
    private String putBlockAddress;
    /**
     * 区块hash值
     */
    private String hash;
    /**
     * 前一个区块的hash值
     */
    private String prevBlockHash;

    /**
     * 区块创建时间(单位:秒)
     */
    private long timeStamp;
    /**
     * 工作量证明计数器
     */
    private long nonce;

    private List<Content> contents = new ArrayList<>();

    private int high;//区块高度

    private String merkleHash;

    /**
     * 交易信息
     */
    private Transaction[] transactions;

    /**
     * <p> 创建创世区块 </p>
     *
     * @param coinbase
     * @return
     */
    public static Block newGenesisBlock(String address,Transaction coinbase) {
        Block block = new Block(address,"", ByteUtils.ZERO_HASH, Instant.now().getEpochSecond(), 0, RocksDBforContent.getInstance().getCauseContents(5),0, "",new Transaction[]{coinbase});

        PoDT podt = PoDT.newProofOfWork(block);
        PoDTResult poDTResult = podt .run();
        block.setHash(poDTResult.getHash());
        block.setNonce(poDTResult.getNonce());
        return block;
        //return Block.newBlock(ByteUtils.ZERO_HASH, new Transaction[]{coinbase},Content.causeGenerate());
    }

    /**
     * <p> 创建新区块 </p>
     *
     * @param previousHash
     *
     * @param transactions
     * @return
     */
    public static Block newBlock(String previousHash, Transaction[] transactions,Content content) {


        Blockchain blockchain = Blockchain.getBlockchain("");
        Block preBlock = RocksDBUtils.getInstance().getBlock(previousHash);

       // Block block = new Block("","", previousHash,  Instant.now().getEpochSecond(), 0, content,preBlock.high+1,"",transactions);
        List<Content> contents = RocksDBforContent.getInstance().getCauseContents(8);
        Block block = new Block("","", RocksDBUtils.getInstance().getLastBlockHash(),  Instant.now().getEpochSecond(), 0, contents,preBlock.high+1,"",transactions);

        PoDT podt = PoDT.newProofOfWork(block);
        PoDTResult poDTResult = podt .run();
        Transaction rewardTx = Transaction.newCoinbaseTX(poDTResult.getAddress(), "");
        UTXOSet utxoSet = new UTXOSet(blockchain);
        utxoSet.updateByTransaction(rewardTx);

        block.setPutBlockAddress(poDTResult.getAddress());

        //todo
        //目前设计 存储节点随机
        Random random = new Random();
        List<WalletDB> walletDBS = RocksDBforWallet.getInstance().getCreditWalletDB();

        for(int i=0;i<10;i++) {
            Content contentRandom = contents.get(random.nextInt(contents.size()));
            String location = (RocksDBforWallet.getInstance().getCreditWalletDB().get(random.nextInt(walletDBS.size()))).getAddress();//随机选择

            //String updateContentWallet = (RocksDBforWallet.getInstance().getCreditWalletDB().get(random.nextInt(walletDBS.size()))).getAddress();//随机选择
            content.setLocation(location);
            //content.setUpdateContentWalletAddress(updateContentWallet);
            RocksDBforContent.getInstance().putContentDB(contentRandom);


            Transaction rewardContentTx = Transaction.newContentRewardTX(location, "", (int) (contentRandom.getStore() / 100));
            utxoSet = new UTXOSet(blockchain);
            utxoSet.updateByTransaction(rewardContentTx);

            RocksDBforWallet.getInstance().addContent(location, contentRandom);
        }

        block.setHash(poDTResult.getHash());
        block.setNonce(poDTResult.getNonce());
        return block;
    }

    /**
     * 对区块中的交易信息进行Hash计算
     *
     * @return
     */
    public byte[] hashTransaction() {
        byte[][] txIdArrays = new byte[this.getTransactions().length][];
        //System.out.println(Arrays.toString(this.getTransactions()));
        for (int i = 0; i < this.getTransactions().length; i++) {
            txIdArrays[i] = this.getTransactions()[i].hash();

        }
        // System.out.println(Arrays.toString(txIdArrays));
        return new MerkleTree(txIdArrays).getRoot().getHash();
    }


}
