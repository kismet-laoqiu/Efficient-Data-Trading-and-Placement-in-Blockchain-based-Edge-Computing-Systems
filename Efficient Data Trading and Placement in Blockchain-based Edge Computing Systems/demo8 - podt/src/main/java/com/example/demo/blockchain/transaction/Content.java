package com.example.demo.blockchain.transaction;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.demo.blockchain.util.SerializeUtils;
import org.apache.commons.codec.digest.DigestUtils;


import java.io.Serializable;
import java.util.Random;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Content implements Serializable {



    public static final String[] dataTypeCause ={"TXT","DOC","PICTURE","VIDEO","RAR"};

    private String contentId;

    private String updateContentWalletAddress;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 大小
     */
    private int store;


    /**
     * 元数据hash
     */
    private byte[] hash;

    /**
     * 区块创建时间(单位:秒)
     */
    private long timeStamp;


    private String description;//描述


    /**
     * 数据位置 钱包
     */
    //private Wallet wallet;
    private String location;



    private long validTime;

    private String IPFSHash;





    /**
     * 计算交易信息的Hash值
     *
     * @return
     */
    public byte[] hash() {
        return DigestUtils.sha256(SerializeUtils.serialize(this));
    }

    public static Content causeGenerate(String id,String IPFSHash) {


        Random rand=new Random();
        int i=(int)(Math.random()*5);
        Content content = new Content(id,null,dataTypeCause[i],rand.nextInt(1000) + 100,null,System.currentTimeMillis(),"description",null,
                System.currentTimeMillis(),IPFSHash);

        //content.setWallet("14JkJyoXdWMjZzuqDRE7eK6XmQZwfKTsUJ");

        content.setLocation("just for test");
        content.setHash(content.hash());

        return content;

    }








}
