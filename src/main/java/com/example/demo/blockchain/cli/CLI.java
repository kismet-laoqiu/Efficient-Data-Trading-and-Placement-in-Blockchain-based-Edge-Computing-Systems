package com.example.demo.blockchain.cli;

import com.alibaba.fastjson.JSON;
import com.example.demo.blockchain.message.Message;
import com.example.demo.blockchain.podt.PoDT;
import com.example.demo.blockchain.store.RocksDBUtils;
import com.example.demo.blockchain.store.RocksDBforContent;
import com.example.demo.blockchain.store.RocksDBforWallet;
import com.example.demo.blockchain.transaction.Content;
import com.example.demo.blockchain.util.*;
import com.example.demo.blockchain.wallet.Wallet;
import com.example.demo.blockchain.block.Block;
import com.example.demo.blockchain.block.Blockchain;
import com.example.demo.blockchain.transaction.TXOutput;
import com.example.demo.blockchain.transaction.Transaction;
import com.example.demo.blockchain.transaction.UTXOSet;
import com.example.demo.blockchain.wallet.WalletDB;
import com.example.demo.blockchain.wallet.WalletUtils;
import com.example.demo.blockchain.websocket.P2PClient;
import com.example.demo.blockchain.websocket.P2PServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class CLI {

    @Autowired
    BlockCache blockCache;

    @Autowired
    P2PServer p2PServer;

    @Autowired
    P2PClient p2PClient;

    @Autowired
    P2PService p2PService;

    //Transaction[] transactions;
    List<Transaction> list = new CopyOnWriteArrayList<>();

    /**
     * 验证入参
     *
     * @param args
     */
    public void validateArgs(String[] args) {
        if (args == null || args.length < 1) {
            help();
        }
    }


    /**
     * 创建区块链
     *
     * @param address
     */
    public void createBlockchain(String address) throws Exception {
        Blockchain blockchain = Blockchain.createBlockchain(address);
        UTXOSet utxoSet = new UTXOSet(blockchain);
        utxoSet.reIndex();
        //log.info("Done ! ");
        this.synBalance();
        Block latestBlock = RocksDBUtils.getInstance().getBlock(blockchain.getLastBlockHash());
        Message msg = new Message();
        msg.setType(BlockConstant.RESPONSE_LATEST_BLOCK);
        msg.setData(JSON.toJSONString(latestBlock));
        p2PService.broatcast(JSON.toJSONString(msg));
        System.out.println("Done!");
    }


    /**
     * 创建钱包
     *
     * @throws Exception
     */
    public void createWallet() throws Exception {
        Wallet wallet = WalletUtils.getInstance().createWallet();
        System.out.println("wallet address : " + wallet.getAddress());
    }

    /**
     * 打印钱包地址
     *
     * @throws Exception
     */
    public void printAddresses() throws Exception {
        Set<String> addresses = WalletUtils.getInstance().getAddresses();
        if (addresses == null || addresses.isEmpty()) {
            System.out.println("There isn't address");
            return;
        }
        for (String address : addresses) {
            System.out.println("Wallet address: " + address);
        }
    }

    /**
     * 查询钱包余额
     *
     * @param address 钱包地址
     */
    public int getBalance(String address) throws Exception {
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(address);
        } catch (Exception e) {
            //log.error("ERROR: invalid wallet address", e);
            System.out.println("ERROR: invalid wallet address"+ e);
            throw new RuntimeException("ERROR: invalid wallet address", e);
        }

        // 得到公钥Hash值
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);

        Blockchain blockchain = Blockchain.getBlockchain(address);
        UTXOSet utxoSet = new UTXOSet(blockchain);

        TXOutput[] txOutputs = utxoSet.findUTXOs(pubKeyHash);
        int balance = 0;
        if (txOutputs != null && txOutputs.length > 0) {
            for (TXOutput txOutput : txOutputs) {
                balance += txOutput.getValue();
            }
        }
        System.out.println("Balance of : {"+address+"} = "+balance);
        return balance;
        //System.out.println("Balance of '{}': {}\n", new Object[]{address, balance});
    }

    /**
     * 获取余额
     * @param address
     * @return
     * @throws Exception
     */
    public int getBalanceNoSout(String address) throws Exception {
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(address);
        } catch (Exception e) {
            //log.error("ERROR: invalid wallet address", e);
            System.out.println("ERROR: invalid wallet address"+ e);
            throw new RuntimeException("ERROR: invalid wallet address", e);
        }

        // 得到公钥Hash值
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);

        Blockchain blockchain = Blockchain.getBlockchain(address);
        UTXOSet utxoSet = new UTXOSet(blockchain);

        TXOutput[] txOutputs = utxoSet.findUTXOs(pubKeyHash);
        int balance = 0;
        if (txOutputs != null && txOutputs.length > 0) {
            for (TXOutput txOutput : txOutputs) {
                balance += txOutput.getValue();
            }
        }
        return balance;
        //System.out.println("Balance of '{}': {}\n", new Object[]{address, balance});
    }

    /**
     *
     * 更新钱包余额
     *
     * @param address 钱包地址
     */
    public int updateWalletBalance(String address) throws Exception {
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(address);
        } catch (Exception e) {
            //log.error("ERROR: invalid wallet address", e);
            System.out.println("ERROR: invalid wallet address"+ e);
            throw new RuntimeException("ERROR: invalid wallet address", e);
        }

        // 得到公钥Hash值
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);
        int balance = this.getBalanceNoSout(address);
        //RocksDBUtils.getInstance().putBalance(address,balance+amount);
        RocksDBforWallet.getInstance().putBalance(address,balance);

        return balance;
        //System.out.println("Balance of '{}': {}\n", new Object[]{address, balance});
    }

    public void synBalance() throws Exception {
        Set<String> set = RocksDBforWallet.getInstance().getWalletKeyset();
        for (String str:set
        ) {

            RocksDBforWallet.getInstance().putBalance(str,this.getBalanceNoSout(str));

        }

    }




    public void send(String from, String to, int amount) throws Exception {
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(from);
        } catch (Exception e) {
            System.out.println("ERROR: sender address invalid ! address=" + from);
            throw new RuntimeException("ERROR: sender address invalid ! address=" + from, e);
        }
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(to);
        } catch (Exception e) {
            System.out.println("ERROR: receiver address invalid ! address=" + from);
            // log.error("ERROR: receiver address invalid ! address=" + to, e);
            throw new RuntimeException("ERROR: receiver address invalid ! address=" + to, e);
        }
        if (amount < 1) {
            // log.error("ERROR: amount invalid ! amount=" + amount);
            System.out.println("ERROR: amount invalid ! amount=" + amount);
            throw new RuntimeException("ERROR: amount invalid ! amount=" + amount);
        }
        Blockchain blockchain = Blockchain.getBlockchain(from);
        // 新交易

        Transaction transaction = Transaction.newUTXOTransaction(from, to, amount, blockchain);
        UTXOSet utxoSet = new UTXOSet(blockchain);
        utxoSet.updateByTransaction(transaction);
        this.updateWalletBalance(from);
        this.updateWalletBalance(to);
        Message msg = new Message();
        msg.setType(BlockConstant.RESPONSE_TRANSACTION);
        msg.setData(JSON.toJSONString(transaction));
        p2PService.broatcast(JSON.toJSONString(msg));

        // 奖励
        //Transaction rewardTx = Transaction.newCoinbaseTX(from, "");
       // Block newBlock = blockchain.mineBlock(new Transaction[]{transaction, rewardTx});
        //new UTXOSet(blockchain).update(newBlock);

        System.out.println("Success!\n");
    }

    public Message addBlockTest(String from) throws Exception {
        Blockchain blockchain = Blockchain.getBlockchain(RocksDBUtils.getInstance().getLastBlockHash());
       //
         //Transaction rewardTx = Transaction.newCoinbaseTX(from, "");
        //Transaction[] transactions = Arrays.copyOf(RocksDBUtils.getInstance().getUndeletedTransactions(),RocksDBUtils.getInstance().getUndeletedTransactions().length+1);
        //transactions[RocksDBUtils.getInstance().getUndeletedTransactions().length]=rewardTx;
        //Transaction[] a = RocksDBUtils.getInstance().getUndeletedTransactions();
        List<Transaction> list = RocksDBUtils.getInstance().getUndeletedTransactions();
        //list.add(rewardTx);
       // list.add(rewardTx);
        //System.out.println(list.toString());
        Block newBlock = blockchain.mineBlock(list.toArray(new Transaction[list.size()]));

        Message msg = new Message();
        msg.setType(BlockConstant.RESPONSE_LATEST_BLOCK);
        msg.setData(JSON.toJSONString(newBlock));
//
        //创建成功后，全网广播出去
        RocksDBUtils.getInstance().deleteAllTransactions(list);
        //this.updateCoinDays(newBlock);
        //new UTXOSet(blockchain).update(newBlock);
        //this.synBalance();
        p2PService.broatcast(JSON.toJSONString(msg));
        return msg;
    }


    public void updateCoinDays(Block newBlock) {
        String address = newBlock.getPutBlockAddress();
        List<WalletDB> walletDBS = RocksDBforWallet.getInstance().getAllWalletDB();
        //String location = newBlock.getContent().getLocation();
        //String location = newBlock.
        //System.out.println("存储资源节点: "+ location+"发块节点： "+address);
        for (WalletDB w: walletDBS
             ) {
            if(w.getAddress().equals(address)) {
                w.setCoinDays(0);
            }else {
                w.setCoinDays(w.getBalance()+w.getCoinDays());
            }

            RocksDBforWallet.getInstance().putWalletDB(w);

        }
    }



    /**
     * 打印帮助信息
     */
    public void help() {
        System.out.println("Usage:");
        System.out.println("  createwallet - Generates a new key-pair and saves it into the wallet file");
        System.out.println("  printaddresses - print all wallet address");
        System.out.println("  getbalance -address ADDRESS - Get balance of ADDRESS");
        System.out.println("  createblockchain -address ADDRESS - Create a blockchain and send genesis block reward to ADDRESS");
        System.out.println("  printchain - Print all the blocks of the blockchain");
        System.out.println("  send -from FROM -to TO -amount AMOUNT - Send AMOUNT of coins from FROM address to TO");
        System.exit(0);
    }

    /**
     * 打印出区块链中的所有区块
     */
    public void printChain() throws Exception {
        Blockchain blockchain = Blockchain.initBlockchainFromDB();
        for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator(); iterator.hashNext(); ) {
            Block block = iterator.next();
            System.out.print((block.getTransactions().length));
            if (block != null) {
                boolean validate = PoDT.newProofOfWork(block).validate();
                System.out.println(block.toString() + ", validate = " + validate);

//            }
            }
        }
    }

    public void printChainDataInCsv() {

        Blockchain blockchain = Blockchain.initBlockchainFromDB();
        List<Block> blockList = new ArrayList<>();
        for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator(); iterator.hashNext(); ) {
            Block block = iterator.next();
            blockList.add(block);
        }
        //System.out.println(blockList.size());
        CsvUtils csvUtils = new CsvUtils();
        csvUtils.exportExcelPaper(blockList);
    }

    public String FirstBlockToJSON() {
        Blockchain blockchain = Blockchain.initBlockchainFromDB();


          Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator();


            Block block = iterator.next();


            if (block != null) {
                boolean validate = PoDT.newProofOfWork(block).validate();
                //System.out.println(block.toString() + ", validate = " + validate);
//            }
            }

        return JSON.toJSONString(block);

    }

    public List<Block> blockChainToJSON() {
        Blockchain blockchain = Blockchain.initBlockchainFromDB();
        List<Block> result = new CopyOnWriteArrayList<>();
        for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator(); iterator.hashNext(); ) {
            Block block = iterator.next();
            System.out.print((block.getTransactions().length));
            if (block != null) {
                boolean validate = PoDT.newProofOfWork(block).validate();
                result.add(block);

//            }
            }
        }
        return result;

    }

    public void showTransactions () {
        RocksDBUtils.getInstance().showTransactions();
    }

    public void TransactionAndAddBlock() {

        try {
            RocksDBforWallet.getInstance().initWalletDB();//初始化钱包
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.createBlockchain("1AZQ6qcmnBMuRU74vy8DsG1FcSzHWrUSBL");
            this.printChainDataInCsv();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<WalletDB> walletDBList = RocksDBforWallet.getInstance().getAllWalletDB();

        for(int i=0;i<105;++i) {
            for(int j=0;j<8;++j) {
                List<WalletDB> walletDBS = RocksDBforWallet.getInstance().getEnoughWalletDB();
                Random rand = new Random();
                int walletDBListTemp = rand.nextInt(walletDBList.size());//rand.nextInt(100) 获得的值是区间 [0, 99]
                int walletDBSTemp = rand.nextInt(walletDBS.size());
                try {
                    //this.send(walletDBS.get(walletDBSTemp).getAddress(),walletDBList.get(walletDBListTemp).getAddress(),1);
                    RocksDBforWallet.getInstance().addTradCount(walletDBS.get(walletDBSTemp).getAddress(),walletDBList.get(walletDBListTemp).getAddress());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            try {
                this.addBlockTest("1AZQ6qcmnBMuRU74vy8DsG1FcSzHWrUSBL");

            } catch (Exception e) {
                e.printStackTrace();
            }
            this.printChainDataInCsv();

            RocksDBforWallet.getInstance().showWalletDB();
            try {
                //this.printChain();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void generateContents() {
        List<String> list = ReadFile.getContent("C:\\Users\\15192\\Desktop\\IPFShash.txt");
        for(int i=1;i<=20;++i) {
            Content content = Content.causeGenerate(Integer.toString(i),list.get(i));
            RocksDBforContent.getInstance().putContentDB(content);
        }
    }

    public void sendP2PTest() {
        try {
            RocksDBforWallet.getInstance().initWalletDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.createBlockchain("1AZQ6qcmnBMuRU74vy8DsG1FcSzHWrUSBL");
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<WalletDB> walletDBList = RocksDBforWallet.getInstance().getAllWalletDB();

        for (int j = 0; j < 5; ++j) {
            List<WalletDB> walletDBS = RocksDBforWallet.getInstance().getEnoughWalletDB();
            Random rand = new Random();
            int walletDBListTemp = rand.nextInt(walletDBList.size());//rand.nextInt(100) 获得的值是区间 [0, 99]
            int walletDBSTemp = rand.nextInt(walletDBS.size());
            try {
                this.send(walletDBS.get(walletDBSTemp).getAddress(), walletDBList.get(walletDBListTemp).getAddress(), 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initTest() {
        try {
            RocksDBforWallet.getInstance().initWalletDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.createBlockchain("1AZQ6qcmnBMuRU74vy8DsG1FcSzHWrUSBL");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void mine() {
            try {
                this.addBlockTest("1AZQ6qcmnBMuRU74vy8DsG1FcSzHWrUSBL");
            } catch (Exception e) {
                e.printStackTrace();
            }

            RocksDBforWallet.getInstance().showWalletDB();
            try {
                this.printChain();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }




}
