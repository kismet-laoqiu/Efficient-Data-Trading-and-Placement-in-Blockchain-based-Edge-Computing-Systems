package com.example.demo.blockchain.podt;

import com.example.demo.blockchain.store.RocksDBforWallet;
import com.example.demo.blockchain.wallet.WalletDB;
import lombok.Data;
import com.example.demo.blockchain.block.Block;
import com.example.demo.blockchain.util.ByteUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

/**
 * podt
 *
 * @author qkmF
 * @date
 */
@Data
public class PoDT {

    /**
     * 难度目标位
     */
    public static final int TARGET_BITS = 20;

    /**
     * 区块
     */
    private Block block;
    /**
     * 难度目标值
     */
    private BigInteger target;

    private PoDT(Block block, BigInteger target) {
        this.block = block;
        this.target = target;
    }

    private PoDT(BigInteger target) {
        this.target = target;
    }

    /**
     * 创建新的工作量证明，设定难度目标值
     * <p>
     * 对1进行移位运算，将1向左移动 (256 - TARGET_BITS) 位，得到我们的难度目标值
     *
     * @param block
     * @return
     */
    public static PoDT newProofOfWork(Block block) {
        BigInteger targetValue = BigInteger.ONE.shiftLeft((256 - TARGET_BITS));
        return new PoDT(block, targetValue);
    }






    /**
     * 运行工作量证明，开始挖矿，找到小于难度目标值的Hash
     *
     * @return
     */
    public PoDTResult run() {
        long nonce = 0;
        String shaHex = "";
        long startTime = System.currentTimeMillis();
        List<WalletDB> walletDBS = RocksDBforWallet.getInstance().getBlockWalletDB();
        double allTrade = 1;
        for (WalletDB walletDB:walletDBS
             ) {
            allTrade+=walletDB.getTradCount();
        }
        Random random = new Random();
        WalletDB randomWalletDB = walletDBS.get(random.nextInt(walletDBS.size()));
        String address = randomWalletDB.getAddress();
        double stake = (double) (randomWalletDB.getCoinDays()+randomWalletDB.getTradCount()*10+randomWalletDB.getCredit()*20)/1500;
        double changeTarget = stake;
        //changeTarget = stake<4 ?4:stake;
        if(changeTarget<2) {
            changeTarget = 2;
        }else if(changeTarget>8) {
            changeTarget = 8;
        }
        System.out.println("changeTarget: "+changeTarget+"stake: "+stake);
        BigDecimal newTarget = new BigDecimal(target);
        newTarget = newTarget.multiply(new BigDecimal(changeTarget));
        while (nonce < Long.MAX_VALUE) {
            byte[] data = this.prepareData(nonce);
            shaHex = DigestUtils.sha256Hex(data);

            //System.out.println("shahex: "+shaHex+" target: "+this.target+" nonce: "+nonce);

            if (new BigInteger(shaHex, 16).compareTo((newTarget.toBigInteger())) < 0) {
                System.out.printf("Elapsed Time: %s seconds \n", (float) (System.currentTimeMillis() - startTime) / 1000);
                System.out.printf("correct hash Hex: %s \n\n", shaHex);
//                WalletDB walletDB = walletDBS.get((int) (walletDBS.size()-(nonce% walletDBS.size())-1));
//                //walletDBS[(int) (walletDBS.length-(nonce% walletDBS.length)-1)];
//                address = walletDB.getAddress();
                System.out.printf("address : %s \n",address);
                break;
            } else {
                nonce++;

            }
        }
        //todo

        List<WalletDB> allWalletDBS = RocksDBforWallet.getInstance().getAllWalletDB();

        for (WalletDB w: allWalletDBS
        ) {
            if(w.getAddress().equals(address)) {
                w.setCoinDays(0);
            }else {
                w.setCoinDays(w.getBalance()+w.getCoinDays());

            }

            RocksDBforWallet.getInstance().putWalletDB(w);


        }
        return new PoDTResult(nonce, shaHex,address);

    }

    /**
     * 验证区块是否有效
     *
     * @return
     */
    public boolean validate() {
        byte[] data = this.prepareData(this.getBlock().getNonce());
        return new BigInteger(DigestUtils.sha256Hex(data), 16).compareTo(this.target) == -1;
    }

    /**
     * 准备数据
     * <p>
     * 注意：在准备区块数据时，一定要从原始数据类型转化为byte[]，不能直接从字符串进行转换
     * @param nonce
     * @return
     */
    private byte[] prepareData(long nonce) {
        byte[] prevBlockHashBytes = {};
        if (StringUtils.isNoneBlank(this.getBlock().getPrevBlockHash())) {
            prevBlockHashBytes = new BigInteger(this.getBlock().getPrevBlockHash(), 16).toByteArray();
        }

        return ByteUtils.merge(
                prevBlockHashBytes,
                this.getBlock().hashTransaction(),
                ByteUtils.toBytes(this.getBlock().getTimeStamp()),
                ByteUtils.toBytes(TARGET_BITS),
                ByteUtils.toBytes(nonce)
        );
    }

}
