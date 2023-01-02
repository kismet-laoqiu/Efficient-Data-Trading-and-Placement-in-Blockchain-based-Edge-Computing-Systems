package com.example.demo.blockchain.cli;

import com.alibaba.fastjson.JSON;
import com.example.demo.blockchain.store.RocksDBUtils;
import com.example.demo.blockchain.store.RocksDBforContent;
import com.example.demo.blockchain.store.RocksDBforWallet;
import com.example.demo.blockchain.wallet.Wallet;
import com.example.demo.blockchain.wallet.WalletDB;
import com.example.demo.blockchain.wallet.WalletUtils;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        CLI cli = new CLI();
        try {
            //cli.generateContents();
            System.out.println((JSON.toJSONString(RocksDBforContent.getInstance().getAllContentDB())));
            //cli.TransactionAndAddBlock();
            //System.out.println((RocksDBforContent.getInstance().getAllContentDB()).toString());
            //System.out.println(RocksDBforContent.getInstance().getCauseContent().toString());
            //cli.createBlockchain("");
            //cli.mine();
            //cli.printChainDataInCsv();

            /*for(int i=0;i<90;++i) {
                System.out.println(i);
                cli.printChainDataInCsv();
                cli.TransactionAndAddBlock();

            }*/

            //cli.TransactionAndAddBlock();
//            cli.createWallet();
//            cli.createWallet();
//            cli.createWallet();
            //RocksDBforWallet.getInstance().initWalletDB();
//
//



            //RocksDBforWallet.getInstance().showWalletDB();
//            List<WalletDB> list = RocksDBforWallet.getInstance().getBlockWalletDB();
//            System.out.println(list.size());
//            for (WalletDB walletDB:list
//            ) {
//
//                System.out.println(walletDB.toString());
//
//            }



           // WalletUtils.getInstance().printAllWallet();

            //cli.showTransactions();
            //RocksDBUtils.getInstance().showTransactions();
           // RocksDBUtils.getInstance().updateBalance();

            //RocksDBUtils.getInstance().showTransactions();
           // cli.printChain();
            //Content content = Content.causeGenerate();
           //System.out.println(content.toString());


            //WalletUtils.getInstance().printAllWallet();


            //cli.showTransactions();

           // cli.printChain();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
