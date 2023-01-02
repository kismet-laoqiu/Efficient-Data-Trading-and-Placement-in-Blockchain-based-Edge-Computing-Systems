package com.example.demo.blockchain.store;

import com.example.demo.blockchain.transaction.Content;
import com.example.demo.blockchain.util.SerializeUtils;
import com.example.demo.blockchain.wallet.WalletDB;
import com.example.demo.blockchain.wallet.WalletUtils;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class RocksDBforContent {

    /**
     * 区块桶前缀
     */
    private static final String Content_BUCKET_KEY = "content";
    /**
     * 区块链数据文件
     */
    private static final String DB_FILE = "content.db";

    private volatile static RocksDBforContent instance;

    public static RocksDBforContent getInstance() {
        if (instance == null) {
            synchronized (RocksDBforContent.class) {
                if (instance == null) {
                    instance = new RocksDBforContent();
                }
            }
        }
        return instance;
    }

    private RocksDB db;

    @Getter
    @Setter
    private Map<String, byte[]> contentBucket;



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
    private void initContentBucket() {
        try {
            byte[]contentBucketKey = SerializeUtils.serialize(Content_BUCKET_KEY);
            byte[] contentBucketBytes = db.get(contentBucketKey);
            if (contentBucketBytes != null) {
                contentBucket = (Map) SerializeUtils.deserialize(contentBucketBytes);
            } else {
                contentBucket = Maps.newHashMap();
                db.put(contentBucketKey, SerializeUtils.serialize(contentBucket));
            }
        } catch (RocksDBException e) {
            throw new RuntimeException("Fail to init contentBucket bucket ! ", e);
        }
    }

    public RocksDBforContent() {
        openDB();
        initContentBucket();
    }

    /**
     * 保存区块
     *
     * @param
     */
    public void putContentDB(Content content) {
        try {
            contentBucket.put(content.getContentId(), SerializeUtils.serialize(content));
            db.put(SerializeUtils.serialize(Content_BUCKET_KEY), SerializeUtils.serialize(contentBucket));
        } catch (RocksDBException e) {
            throw new RuntimeException("Fail to put content ! ", e);
        }
    }

    public Content getContentDB(String ContentId) {
        return (Content) SerializeUtils.deserialize(contentBucket.get(ContentId));
    }

//    /**
//     * 保存区块
//     *
//     * @param
//     */
//    public void putBalance(String walletAddress,int balance) {
//        WalletDB walletDB = this.getWalletDB(walletAddress);
//        walletDB.setBalance(balance);
//        try {
//            walletBucket.put(walletAddress, SerializeUtils.serialize(walletDB));
//            db.put(SerializeUtils.serialize(WALLET_BUCKET_KEY), SerializeUtils.serialize(walletBucket));
//        } catch (RocksDBException e) {
//            throw new RuntimeException("Fail to put balance ! ", e);
//        }
//    }
//
//    /**
//     * 查询区块
//     *
//     * @param
//     * @return
//     */
//    public int getBalance(String walletAddress) {
//        WalletDB walletDB = this.getWalletDB(walletAddress);
//        return (walletDB.getBalance());
//    }

    public void showWalletDB() {

        System.out.println(contentBucket.keySet().size());
        for (String str:contentBucket.keySet()
        ) {

            System.out.println(getContentDB(str).toString());

        }
    }

    public List<Content> getAllContentDB() {
        List<Content> list = new CopyOnWriteArrayList<>();
        for (String str:contentBucket.keySet()
        ) {

            list.add(this.getContentDB(str));
        }

        return list;

    }




    public  Set<String> getWalletKeyset() {
        return contentBucket.keySet();
    }

    public Content getCauseContent() {
        List<Content> list = this.getAllContentDB();
        Random random = new Random();
        int i = random.nextInt(list.size());
        return list.get(i);
    }

    public List<Content> getCauseContents(int size) {
        List<Content> list = this.getAllContentDB();
        List<Content> causeList = new ArrayList<>();
        if(size<list.size()) {
            for(int i=0;i<size;++i) {
                Random random = new Random();
                int temp = random.nextInt(list.size());
                causeList.add(list.get(temp));
            }
            return causeList;
        }else {
            return list;
        }

    }

}
