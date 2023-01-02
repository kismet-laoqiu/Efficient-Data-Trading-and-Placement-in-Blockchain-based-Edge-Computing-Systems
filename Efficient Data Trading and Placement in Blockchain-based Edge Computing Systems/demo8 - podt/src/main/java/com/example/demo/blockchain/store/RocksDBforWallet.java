package com.example.demo.blockchain.store;

import com.alibaba.fastjson.JSON;
import com.example.demo.blockchain.block.Block;
import com.example.demo.blockchain.transaction.Content;
import com.example.demo.blockchain.util.SerializeUtils;
import com.example.demo.blockchain.wallet.Wallet;
import com.example.demo.blockchain.wallet.WalletDB;
import com.example.demo.blockchain.wallet.WalletUtils;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class RocksDBforWallet {

    /**
     * 区块桶前缀
     */
    private static final String WALLET_BUCKET_KEY = "wallet";
    /**
     * 区块链数据文件
     */
    private static final String DB_FILE = "wallet.db";

    private volatile static RocksDBforWallet instance;

    public static RocksDBforWallet getInstance() {
        if (instance == null) {
            synchronized (RocksDBforWallet.class) {
                if (instance == null) {
                    instance = new RocksDBforWallet();
                }
            }
        }
        return instance;
    }

    private RocksDB db;

    @Getter
    @Setter
    private Map<String, byte[]> walletBucket;



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
    private void initWalletBucket() {
        try {
            byte[]walletBucketKey = SerializeUtils.serialize(WALLET_BUCKET_KEY);
            byte[] walletBucketBytes = db.get(walletBucketKey);
            if (walletBucketBytes != null) {
                walletBucket = (Map) SerializeUtils.deserialize(walletBucketBytes);
            } else {
                walletBucket = Maps.newHashMap();
                db.put(walletBucketKey, SerializeUtils.serialize(walletBucket));
            }
        } catch (RocksDBException e) {
            throw new RuntimeException("Fail to init wallet bucket ! ", e);
        }
    }

    public RocksDBforWallet() {
        openDB();
        initWalletBucket();
    }

    /**
     * 保存区块
     *
     * @param
     */
    public void putWalletDB(WalletDB walletDB) {
        try {
            walletBucket.put(walletDB.getAddress(), SerializeUtils.serialize(walletDB));
            db.put(SerializeUtils.serialize(WALLET_BUCKET_KEY), SerializeUtils.serialize(walletBucket));
        } catch (RocksDBException e) {
            throw new RuntimeException("Fail to put block ! ", e);
        }
    }

    public WalletDB getWalletDB(String address) {
        return (WalletDB) SerializeUtils.deserialize(walletBucket.get(address));
    }

    /**
     * 保存区块
     *
     * @param
     */
    public void putBalance(String walletAddress,int balance) {
        WalletDB walletDB = this.getWalletDB(walletAddress);
        walletDB.setBalance(balance);
        try {
            walletBucket.put(walletAddress, SerializeUtils.serialize(walletDB));
            db.put(SerializeUtils.serialize(WALLET_BUCKET_KEY), SerializeUtils.serialize(walletBucket));
        } catch (RocksDBException e) {
            throw new RuntimeException("Fail to put balance ! ", e);
        }
    }

    /**
     * 查询区块
     *
     * @param
     * @return
     */
    public int getBalance(String walletAddress) {
        WalletDB walletDB = this.getWalletDB(walletAddress);
        return (walletDB.getBalance());
    }

    public void showWalletDB() {

        System.out.println(walletBucket.keySet().size());
        for (String str:walletBucket.keySet()
        ) {

            System.out.println(getWalletDB(str).toString());

        }
    }

    public List<WalletDB> getAllWalletDB() {
        List<WalletDB> list = new CopyOnWriteArrayList<>();
        for (String str:walletBucket.keySet()
        ) {

            list.add(this.getWalletDB(str));
        }


        return list;

    }

    public List<WalletDB> getEnoughWalletDB() {
        List<WalletDB> list = new CopyOnWriteArrayList<>();
        for (String str:walletBucket.keySet()
        ) {
            if(this.getBalance(str)>0) {
                list.add(this.getWalletDB(str));
            }
        }
        return list;

    }

    public void initWalletDB() throws Exception {
        Set<String> set = WalletUtils.getInstance().getAddresses();

        for (String str:set
        ) {
            WalletDB walletDB = new WalletDB(200000,200000,500,str,0,0,10,0,null);
            putWalletDB(walletDB);

        }
    }

    public  Set<String> getWalletKeyset() {
        return walletBucket.keySet();
    }

    public void addContent(String address,Content content) {
        List<Content> list;
        WalletDB walletDB = this.getWalletDB(address);
        if(walletDB.getContents()==null||walletDB.getContents().size()==0) {
            list = new CopyOnWriteArrayList<>();
            list.add(content);
            walletDB.setContents(list);
        }else {
            List<Content> res = walletDB.getContents();
           res.add(content);
           walletDB.setContents(res);

        }
        walletDB.setStore(walletDB.getStore()-content.getStore());
        walletDB.setTradCount(walletDB.getTradCount()+1);
        this.putWalletDB(walletDB);
    }

    public void addTradCount(String address1,String address2) {
        WalletDB walletDB1 = this.getWalletDB(address1);
        WalletDB walletDB2 = this.getWalletDB(address2);
        walletDB1.setTradCount(walletDB1.getTradCount()+1);
        walletDB2.setTradCount(walletDB2.getTradCount()+1);
    }

    public List<WalletDB> getBlockWalletDB() {
        List<WalletDB> list = new CopyOnWriteArrayList<>();
        for (String str:walletBucket.keySet()
        ) {
            WalletDB walletDB = this.getWalletDB(str);
            if(walletDB.getCredit()>=7) {
                list.add(walletDB);
            }
        }
        list.sort((WalletDB o1, WalletDB o2) -> (o2.getCoinDays()+o2.getTradCount()*10) - (o1.getCoinDays()+o1.getTradCount()*10));
        List<WalletDB> resultList = new CopyOnWriteArrayList<>();
        for(int i=0;i<3;++i) {
            resultList.add(list.get(i));
        }



        return resultList;

    }

    public List<WalletDB> getCreditWalletDB() {
        List<WalletDB> list = new CopyOnWriteArrayList<>();
        for (String str:walletBucket.keySet()
        ) {
            WalletDB walletDB = this.getWalletDB(str);
            if(walletDB.getCredit()>=7) {
                list.add(walletDB);
            }
        }

        return list;

    }


}
