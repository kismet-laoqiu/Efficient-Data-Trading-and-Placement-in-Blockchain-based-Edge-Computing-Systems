package com.example.demo.blockchain.store;

import com.example.demo.blockchain.block.Block;
import com.example.demo.blockchain.cli.CLI;
import com.example.demo.blockchain.transaction.TXOutput;
import com.example.demo.blockchain.transaction.Transaction;
import com.example.demo.blockchain.util.SerializeUtils;
import com.example.demo.blockchain.wallet.WalletUtils;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class RocksDBUtils {

    /**
     * 区块链数据文件
     */
    private static final String DB_FILE = "blockchain.db";
    /**
     * 区块桶前缀
     */
    private static final String BLOCKS_BUCKET_KEY = "blocks";

    /**
     * 链状态桶Key
     */
    private static final String CHAINSTATE_BUCKET_KEY = "chainstate";

    /**
     * 交易桶
     */

    /**
     * 链状态桶Key
     */
    private static final String TRANSACTION_BUCKET_KEY = "transaction";

    /**
     * wallet Key
     */
    private static final String WALLET_BUCKET_KEY = "wallet_balance";

    /**
     * 最新一个区块
     */
    private static final String LAST_BLOCK_KEY = "l";

    private volatile static RocksDBUtils instance;

    public static RocksDBUtils getInstance() {
        if (instance == null) {
            synchronized (RocksDBUtils.class) {
                if (instance == null) {
                    instance = new RocksDBUtils();
                }
            }
        }
        return instance;
    }

    private RocksDB db;

    /**
     * block buckets
     */
    private Map<String, byte[]> blocksBucket;

    /**
     * chainstate buckets
     */
    @Getter
    private Map<String, byte[]> chainstateBucket;

    /**
     * chainstate buckets
     */
    @Getter
    @Setter
    private Map<String, byte[]> transactionBucket;

    @Getter
    @Setter
    private Map<String, byte[]> wallet_balanceBucket;

    public RocksDBUtils() {
        openDB();
        initBlockBucket();
        initChainStateBucket();
        initTransactionBucket();
        initWallet_balanceBucket();
    }

    /**
     * 打开数据库
     */
    private void openDB() {
        try {
            db = RocksDB.open(DB_FILE);
        } catch (RocksDBException e) {
            throw new RuntimeException("Fail to open db ! ", e);
        }
    }

    /**
     * 初始化 blocks 数据桶
     */
    private void initBlockBucket() {
        try {
            byte[] blockBucketKey = SerializeUtils.serialize(BLOCKS_BUCKET_KEY);
            byte[] blockBucketBytes = db.get(blockBucketKey);
            if (blockBucketBytes != null) {
                blocksBucket = (Map) SerializeUtils.deserialize(blockBucketBytes);
            } else {
                blocksBucket = Maps.newHashMap();
                db.put(blockBucketKey, SerializeUtils.serialize(blocksBucket));
            }
        } catch (RocksDBException e) {
            throw new RuntimeException("Fail to init block bucket ! ", e);
        }
    }

    /**
     * 初始化 blocks 数据桶
     */
    private void initChainStateBucket() {
        try {
            byte[] chainstateBucketKey = SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY);
            byte[] chainstateBucketBytes = db.get(chainstateBucketKey);
            if (chainstateBucketBytes != null) {
                chainstateBucket = (Map) SerializeUtils.deserialize(chainstateBucketBytes);
            } else {
                chainstateBucket = Maps.newHashMap();
                db.put(chainstateBucketKey, SerializeUtils.serialize(chainstateBucket));
            }
        } catch (RocksDBException e) {
            //log.error("Fail to init chainstate bucket ! ", e);
            System.out.println("Fail to init chainstate bucket ! "+e);
            throw new RuntimeException("Fail to init chainstate bucket ! ", e);
        }
    }

    /**
     * 初始化 transaction 数据桶
     */
    private void initTransactionBucket() {
        try {
            byte[] transactionBucketKey = SerializeUtils.serialize(TRANSACTION_BUCKET_KEY);
            byte[] transactionBucketBytes = db.get(transactionBucketKey);
            if (transactionBucketBytes != null) {
                transactionBucket = (Map) SerializeUtils.deserialize(transactionBucketBytes);
            } else {
                transactionBucket = Maps.newHashMap();
                db.put(transactionBucketKey, SerializeUtils.serialize(transactionBucket));
            }
        } catch (RocksDBException e) {
            //log.error("Fail to init chainstate bucket ! ", e);
            System.out.println("Fail to init transaction bucket ! "+e);
            throw new RuntimeException("Fail to init transaction bucket ! ", e);
        }
    }

    /**
     * 初始化 transaction 数据桶
     */
    private void initWallet_balanceBucket() {
        try {
            byte[] walletBucketKey = SerializeUtils.serialize(WALLET_BUCKET_KEY);
            byte[] walletBucketBytes = db.get(walletBucketKey);
            if (walletBucketBytes != null) {
                wallet_balanceBucket = (Map) SerializeUtils.deserialize(walletBucketBytes);
            } else {
                wallet_balanceBucket = Maps.newHashMap();
                db.put(walletBucketKey, SerializeUtils.serialize(wallet_balanceBucket));
            }
        } catch (RocksDBException e) {
            //log.error("Fail to init chainstate bucket ! ", e);
            System.out.println("Fail to init wallet_balanceBucket  ! "+e);
            throw new RuntimeException("Fail to init wallet_balanceBucket ! ", e);
        }
    }

    /**
     * 保存最新一个区块的Hash值
     *
     * @param tipBlockHash
     */
    public void putLastBlockHash(String tipBlockHash) {
        try {
            blocksBucket.put(LAST_BLOCK_KEY, SerializeUtils.serialize(tipBlockHash));
            db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY), SerializeUtils.serialize(blocksBucket));
        } catch (RocksDBException e) {
            throw new RuntimeException("Fail to put last block hash ! ", e);
        }
    }

    /**
     * 查询最新一个区块的Hash值
     *
     * @return
     */
    public String getLastBlockHash() {
        byte[] lastBlockHashBytes = blocksBucket.get(LAST_BLOCK_KEY);
        if (lastBlockHashBytes != null) {
            return (String) SerializeUtils.deserialize(lastBlockHashBytes);
        }
        return "";
    }

    /**
     * 保存区块
     *
     * @param block
     */
    public void putBlock(Block block) {
        try {
            //System.out.println("block hash: "+block.getHash());
            blocksBucket.put(block.getHash(), SerializeUtils.serialize(block));
            db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY), SerializeUtils.serialize(blocksBucket));
        } catch (RocksDBException e) {
            throw new RuntimeException("Fail to put block ! ", e);
        }
    }

    /**
     * 查询区块
     *
     * @param blockHash
     * @return
     */
    public Block getBlock(String blockHash) {
        return (Block) SerializeUtils.deserialize(blocksBucket.get(blockHash));
    }

    /**
     * 清空chainstate bucket
     */
    public void cleanChainStateBucket() {
        try {
            chainstateBucket.clear();
        } catch (Exception e) {
            //log.error("Fail to clear chainstate bucket ! ", e);
            System.out.println("Fail to clear chainstate bucket ! "+e);
            throw new RuntimeException("Fail to clear chainstate bucket ! ", e);
        }
    }

    /**
     * 保存UTXO数据
     *
     * @param key   交易ID
     * @param utxos UTXOs
     */
    public void putUTXOs(String key, TXOutput[] utxos) {
        try {
            chainstateBucket.put(key, SerializeUtils.serialize(utxos));
            db.put(SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY), SerializeUtils.serialize(chainstateBucket));
        } catch (Exception e) {
            System.out.println("Fail to put UTXOs into chainstate bucket ! key=" + key);
           // log.error("Fail to put UTXOs into chainstate bucket ! key=" + key, e);
            throw new RuntimeException("Fail to put UTXOs into chainstate bucket ! key=" + key, e);
        }
    }


    /**
     * 查询UTXO数据
     *
     * @param key 交易ID
     */
    public TXOutput[] getUTXOs(String key) {
        byte[] utxosByte = chainstateBucket.get(key);
        if (utxosByte != null) {
            return (TXOutput[]) SerializeUtils.deserialize(utxosByte);
        }
        return null;
    }


    /**
     * 删除 UTXO 数据
     *
     * @param key 交易ID
     */
    public void deleteUTXOs(String key) {
        try {
            chainstateBucket.remove(key);
            db.put(SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY), SerializeUtils.serialize(chainstateBucket));
        } catch (Exception e) {
            //
            // log.error("Fail to delete UTXOs by key ! key=" + key, e);
            throw new RuntimeException("Fail to delete UTXOs by key ! key=" + key, e);
        }
    }



    /**
     * 关闭数据库
     */
    public void closeDB() {
        try {
            db.close();
        } catch (Exception e) {
            throw new RuntimeException("Fail to close db ! ", e);
        }
    }


    /**
     * ******************************************************************************
     */

    /**
     * 保存最新一个区块的Hash值
     *
     * @param tipBlockHash
     */
//    public void putLastTransaction(String tipBlockHash) {
//        try {
//            blocksBucket.put(LAST_BLOCK_KEY, SerializeUtils.serialize(tipBlockHash));
//            db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY), SerializeUtils.serialize(blocksBucket));
//        } catch (RocksDBException e) {
//            throw new RuntimeException("Fail to put last block hash ! ", e);
//        }
//    }

    /**
     * 查询最新一个区块的Hash值
     *
     * @return
     */
//    public String getLastBlockHash() {
//        byte[] lastBlockHashBytes = blocksBucket.get(LAST_BLOCK_KEY);
//        if (lastBlockHashBytes != null) {
//            return (String) SerializeUtils.deserialize(lastBlockHashBytes);
//        }
//        return "";
//    }

    /**
     * 保存区块
     *
     * @param transaction
     */
    public void putTransaction(Transaction transaction) {
        try {
            transactionBucket.put(Arrays.toString(transaction.getTxId()), SerializeUtils.serialize(transaction));
            db.put(SerializeUtils.serialize(TRANSACTION_BUCKET_KEY), SerializeUtils.serialize(transactionBucket));
        } catch (RocksDBException e) {
            throw new RuntimeException("Fail to put transaction ! ", e);
        }
    }

    /**
     * 查询区块
     *
     * @param str
     * @return
     */
    public Transaction getTransaction(String str) {
        return (Transaction) SerializeUtils.deserialize(transactionBucket.get(str));
    }

    public List<Transaction> getUndeletedTransactions() {
        List<Transaction> result = new CopyOnWriteArrayList<>();

        for (String str:transactionBucket.keySet()
        ) {
            result.add(getTransaction(str));
        }
        return result;

    }

    /**
     * 清空chainstate bucket
     */
    public void cleanTransactionBucket() {
        try {
            this.transactionBucket.clear();
            System.out.println("clean");
        } catch (Exception e) {
            //log.error("Fail to clear chainstate bucket ! ", e);
            System.out.println("Fail to clear transactionBucket  ! "+e);
            throw new RuntimeException("Fail to clear transactionBucket  ! ", e);
        }
    }

    public void showAll() {
        RocksIterator iter= db.newIterator();
        for (iter.seekToFirst(); iter.isValid();iter.next()) {
            System.out.println(new String(iter.key())+","+iter.value().toString());
        }
    }

    /**
     * 删除 UTXO 数据
     *
     * @param
     */
    public void deleteAllTransactions(List<Transaction> list) {
        for (Transaction tx:list
             ) {
            this.deleteTransaction(Arrays.toString(tx.getTxId()));

        }
    }


    public void deleteTransaction(String key) {
        try {
            transactionBucket.remove(key);
            db.put(SerializeUtils.serialize(TRANSACTION_BUCKET_KEY), SerializeUtils.serialize(transactionBucket));
        } catch (Exception e) {
            //
            // log.error("Fail to delete UTXOs by key ! key=" + key, e);
            throw new RuntimeException("Fail to deleteTransactions by key ! key=" + key, e);
        }
    }

    public void soutUndeletedTransactions() {
        List<Transaction> result = new CopyOnWriteArrayList<>();

        for (String str:transactionBucket.keySet()
        ) {
            System.out.println(getTransaction(str).toString());
        }


    }


    public void showTransactions() {
        //System.out.println(transactionBucket.keySet().toString());

       // System.out.println(this.transactionBucket.toString());
        for (String str:transactionBucket.keySet()
             ) {
            System.out.println(str);
            System.out.println(getTransaction(str));

        }
    }










}
