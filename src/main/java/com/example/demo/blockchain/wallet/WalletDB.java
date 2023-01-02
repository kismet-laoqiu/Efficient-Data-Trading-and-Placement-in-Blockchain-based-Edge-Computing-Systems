package com.example.demo.blockchain.wallet;


import com.example.demo.blockchain.transaction.Content;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletDB {

    private long store;//可使用容量

    private long power;//电量

    private int wifi;//k/s网速

    private String address;



    private int balance;//余额

    private int coinDays;//币龄

    private int credit;//信誉

    private int tradCount;


    private List<Content> contents;

}
