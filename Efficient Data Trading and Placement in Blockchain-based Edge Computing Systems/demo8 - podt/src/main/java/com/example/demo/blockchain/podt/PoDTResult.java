package com.example.demo.blockchain.podt;

import lombok.Data;

/**
 * <p> 计算结果 </p>
 */
@Data
public class PoDTResult {

    /**
     * 计数器
     */
    private long nonce;
    /**
     * hash值
     */
    private String hash;

    private String address;

    public PoDTResult(long nonce, String hash, String address) {
        this.nonce = nonce;
        this.hash = hash;
        this.address = address;
    }
}
