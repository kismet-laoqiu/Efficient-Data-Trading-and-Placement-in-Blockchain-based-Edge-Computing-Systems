package com.example.demo.blockchain.wallet;

import lombok.Cleanup;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;

public class GraphUtils {

    /**
     * 钱包文件
     */
    private final static String GRAPH_FILE = "graph.dat";

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";
    /**
     * 密文
     */
    private static final byte[] CIPHER_TEXT = "2oF@5sC%DNf32y!TmiZi!tG9W5rLaniD".getBytes();


    /**
     * 钱包工具实例
     */
    private volatile static GraphUtils instance;

    public static GraphUtils getInstance() {
        if (instance == null) {
            synchronized (GraphUtils.class) {
                if (instance == null) {
                    instance = new GraphUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化钱包文件
     */
    private void initWalletFile() {
        File file = new File(GRAPH_FILE);
        if (!file.exists()) {
            this.saveToDisk(new Graph());
        } else {
            this.loadFromDisk();
        }
    }

    /**
     * 保存钱包数据
     */
    private void saveToDisk(Graph graph) {
        try {
            if (graph == null) {
                throw new Exception("ERROR: Fail to save wallet to file ! data is null ! ");
            }
            SecretKeySpec sks = new SecretKeySpec(CIPHER_TEXT, ALGORITHM);
            // Create cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, sks);
            SealedObject sealedObject = new SealedObject(graph, cipher);
            // Wrap the output stream
            @Cleanup CipherOutputStream cos = new CipherOutputStream(
                    new BufferedOutputStream(new FileOutputStream(GRAPH_FILE)), cipher);
            @Cleanup ObjectOutputStream outputStream = new ObjectOutputStream(cos);
            outputStream.writeObject(sealedObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载钱包数据
     */
    private Graph loadFromDisk() {
        try {
            SecretKeySpec sks = new SecretKeySpec(CIPHER_TEXT, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, sks);
            @Cleanup CipherInputStream cipherInputStream = new CipherInputStream(
                    new BufferedInputStream(new FileInputStream(GRAPH_FILE)), cipher);
            @Cleanup ObjectInputStream inputStream = new ObjectInputStream(cipherInputStream);
            SealedObject sealedObject = (SealedObject) inputStream.readObject();
            return (Graph) sealedObject.getObject(cipher);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Fail to load wallet file from disk ! ");
    }

    public Graph getGraph() {
        Graph graph = this.loadFromDisk();
        return graph;

    }

    public Graph createGraph() {
        Graph graph = new Graph();
        this.saveToDisk(graph);
        return graph;

    }


}
