package com.example.demo.blockchain.cli;


import com.alibaba.fastjson.JSON;
import com.example.demo.blockchain.block.Block;
import com.example.demo.blockchain.block.Blockchain;
import com.example.demo.blockchain.message.Message;
import com.example.demo.blockchain.podt.PoDT;
import com.example.demo.blockchain.store.RocksDBUtils;
import com.example.demo.blockchain.store.RocksDBforWallet;
import com.example.demo.blockchain.transaction.Transaction;
import com.example.demo.blockchain.transaction.UTXOSet;
import com.example.demo.blockchain.util.BlockCache;
import com.example.demo.blockchain.util.BlockConstant;
import com.example.demo.blockchain.wallet.WalletDB;
import com.example.demo.blockchain.websocket.P2PClient;
import com.example.demo.blockchain.websocket.P2PServer;
import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class P2PService implements ApplicationRunner {

    /**
     * 客户端和服务端共用的消息处理方法
     * @param webSocket
     * @param msg
     * @param sockets
     */

    @Autowired
    CLI cli;

    @Autowired
    BlockCache blockCache;

    @Autowired
    P2PServer p2PServer;

    @Autowired
    P2PClient p2PClient;


    public void handleMessage(WebSocket webSocket, String msg, List<WebSocket> sockets) {
        try {
            Message message = JSON.parseObject(msg, Message.class);
            System.out.println("接收到IP地址为：" +webSocket.getRemoteSocketAddress().getAddress().toString()
                    +"，端口号为："+ webSocket.getRemoteSocketAddress().getPort() + "的p2p消息："
                    + JSON.toJSONString(message));
            switch (message.getType()) {
                //客户端请求查询最新的区块:1
                case BlockConstant.QUERY_LATEST_BLOCK:
                    write(webSocket, responseLatestBlockMsg());//服务端调用方法返回最新区块:2
                    break;
                //接收到服务端返回的最新区块:2
                case BlockConstant.RESPONSE_LATEST_BLOCK:
                    handleBlockResponse(message.getData(), sockets);
                    break;
//                //客户端请求查询整个区块链:3
                case BlockConstant.QUERY_BLOCKCHAIN:
                    write(webSocket, responseBlockChainMsg());//服务端调用方法返回最新区块:4
                    break;
                //直接接收到其他节点发送的整条区块链信息:4
                case BlockConstant.RESPONSE_BLOCKCHAIN:
                    handleBlockChainResponse(message.getData(), sockets);
                    break;
//                case BlockConstant.QUERY_TRANSACTION:
//                    write(webSocket, responseUndeletedTransaction());
//                    break;
                case BlockConstant.RESPONSE_TRANSACTION:
                    handleTransactionResponse(message.getData(), sockets);
                    break;
            }
        } catch (Exception e) {
            System.out.println("处理IP地址为：" +webSocket.getRemoteSocketAddress().getAddress().toString()
                    +"，端口号为："+ webSocket.getRemoteSocketAddress().getPort() + "的p2p消息错误:"
                    + e.getMessage());
        }
    }
    /**
     * 处理交易
     */
    public synchronized void handleTransactionResponse(String transactionData,List<WebSocket> sockets) {
        System.out.println("处理handleTransactionResponse");
        Transaction transaction = JSON.parseObject(transactionData,Transaction.class);
        Blockchain blockchain = Blockchain.getBlockchain("");
        UTXOSet utxoSet = new UTXOSet(blockchain);
        utxoSet.updateByTransaction(transaction);
//        cli.updateWalletBalance();
//        cli.updateWalletBalance(to);
        RocksDBUtils.getInstance().putTransaction(transaction);
        List<WalletDB> list = RocksDBforWallet.getInstance().getAllWalletDB();
        for (WalletDB walletdb:list
             ) {
            try {
                cli.updateWalletBalance(walletdb.getAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    /**
     * 处理其它节点发送过来的区块信息
     * @param blockData
     * @param sockets
     */
    public synchronized void handleBlockResponse(String blockData, List<WebSocket> sockets) {
        System.out.println("处理handleBlockResponse");
        //反序列化得到其它节点的最新区块信息
        Block latestBlockReceived = JSON.parseObject(blockData, Block.class);
        //当前节点的最新区块
        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();

        Block latestBlock = RocksDBUtils.getInstance().getBlock(lastBlockHash);



        if (latestBlockReceived != null) {
            if(latestBlock != null) {
                //如果接收到的区块高度比本地区块高度大的多
                if(latestBlockReceived.getHigh() > latestBlock.getHigh() + 1) {
                    broatcast(queryBlockChainMsg());
                    System.out.println("重新查询所有节点上的整条区块链");
                }else if (latestBlockReceived.getHigh() > latestBlock.getHigh() &&
                        latestBlock.getHash().equals(latestBlockReceived.getPrevBlockHash())) {
//                    if (blockService.addBlock(latestBlockReceived)) {
//                        broatcast(responseLatestBlockMsg());
//                    }
                    System.out.println("block hash: "+latestBlockReceived.getHash());
                    RocksDBUtils.getInstance().putBlock(latestBlockReceived);
                    RocksDBUtils.getInstance().putLastBlockHash(latestBlockReceived.getHash());
                    broatcast(responseLatestBlockMsg());

                    System.out.println("将新接收到的区块加入到本地的区块链");
                }
            }else if(latestBlock == null) {
                broatcast(queryBlockChainMsg());
                System.out.println("重新查询所有节点上的整条区块链");
            }
        }
    }

    /**
     * 处理其它节点发送过来的区块链信息
     * @param blockData
     * @param sockets
     */
    public synchronized void handleBlockChainResponse(String blockData, List<WebSocket> sockets) {
        //反序列化得到其它节点的整条区块链信息
        List<Block> receiveBlockchain = JSON.parseArray(blockData, Block.class);
        System.out.println(receiveBlockchain.size());
        if (!CollectionUtils.isEmpty(receiveBlockchain) /*&& blockService.isValidChain(receiveBlockchain)*/) {
            //根据区块索引先对区块进行排序  高度
//            RocksDBUtils.getInstance().putLastBlockHash(receiveBlockchain.get(0).getHash());
////            Collections.sort(receiveBlockchain, new Comparator<Block>() {
////                public int compare(Block block1, Block block2) {
////                    return block1.getHigh() - block2.getHigh();
////                }
////            });
//            for (Block block: receiveBlockchain
//            ) {
//                RocksDBUtils.getInstance().putBlock(block);
//
//            }

//            //其它节点的最新区块
            Block latestBlockReceived = receiveBlockchain.get(0);
//            //当前节点的最新区块
            String latestOwnBlockChainHash = RocksDBUtils.getInstance().getLastBlockHash();
            Block latestOwnBlock = RocksDBUtils.getInstance().getBlock(latestOwnBlockChainHash);
//
            if (latestOwnBlock == null) {
                //替换本地的区块链
                //
                for (Block block : receiveBlockchain
                ) {
                    RocksDBUtils.getInstance().putBlock(block);

                }
                RocksDBUtils.getInstance().putLastBlockHash(receiveBlockchain.get(0).getHash());
            } else {
//                //其它节点区块链如果比当前节点的长，则处理当前节点的区块链
               if(latestBlockReceived.getHigh() > latestOwnBlock.getHigh()) {
                   for (Block block : receiveBlockchain
                   ) {
                       RocksDBUtils.getInstance().putBlock(block);

                   }
                   RocksDBUtils.getInstance().putLastBlockHash(receiveBlockchain.get(0).getHash());
            }
            }
        }
    }



    /**
     * 全网广播消息
     * @param message
     */
    public void broatcast(String message) {
        List<WebSocket> socketsList = this.getSockets();
        if (CollectionUtils.isEmpty(socketsList)) {
            return;
        }
        System.out.println("======全网广播消息开始：");
        for (WebSocket socket : socketsList) {
            this.write(socket, message);
        }
        System.out.println("======全网广播消息结束");
    }

    /**
     * 向其它节点发送消息
     * @param ws
     * @param message
     */
    public void write(WebSocket ws, String message) {
        System.out.println("发送给IP地址为：" +ws.getRemoteSocketAddress().getAddress().toString()
                + "，端口号为："+ws.getRemoteSocketAddress().getPort() + " 的p2p消息:" + message);
        ws.send(message);
    }

    /**
     * 查询整条区块链
     * @return
     */
    public String queryBlockChainMsg() {
        return JSON.toJSONString(new Message(BlockConstant.QUERY_BLOCKCHAIN));
    }

    /**
     * 返回整条区块链数据
     * @return
     */
    public String responseBlockChainMsg() {
        Message msg = new Message();
        msg.setType(BlockConstant.RESPONSE_BLOCKCHAIN);
        //msg.setData(JSON.toJSONString(blockCache.getBlockChain()));
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
        msg.setData(JSON.toJSONString(result));
        return JSON.toJSONString(msg);
    }

    /**
     * 查询最新的区块
     * @return
     */
    public String queryLatestBlockMsg() {
        return JSON.toJSONString(new Message(BlockConstant.QUERY_LATEST_BLOCK));
    }



    /**
     * 返回最新的区块
     * @return
     */
    public String responseLatestBlockMsg() {
        Message msg = new Message();
        msg.setType(BlockConstant.RESPONSE_LATEST_BLOCK);
        //Block b = blockCache.getLatestBlock();
        //Block b = RocksDBUtils.getInstance().getLastBlockHash();
        Block b = RocksDBUtils.getInstance().getBlock(RocksDBUtils.getInstance().getLastBlockHash());
        msg.setData(JSON.toJSONString(b));
        return JSON.toJSONString(msg);
    }


//    public String responseUndeletedTransaction() {
//        Message msg = new Message();
//        msg.setType(BlockConstant.RESPONSE_TRANSACTION);
//        List<Transaction> list = RocksDBUtils.getInstance().getUndeletedTransactions();
//        msg.setData(JSON.toJSONString(list));
//        return JSON.toJSONString(msg);
//
//
//    }

//    public String responseAllWalletDBMsg() {
//        Message msg = new Message();
//        msg.setType(BlockConstant.RESPONSE_WALLET);
//        List<WalletDB> list = RocksDBforWallet.getInstance().getAllWalletDB();
//        msg.setData(JSON.toJSONString(list));
//        return JSON.toJSONString(list);
//    }

    public List<WebSocket> getSockets(){
        return blockCache.getSocketsList();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        p2PServer.initP2PServer(blockCache.getP2pport());
        p2PClient.connectToPeer(blockCache.getAddress());
        System.out.println("*****难度系数******"+blockCache.getDifficulty());
        System.out.println("*****端口号******"+blockCache.getP2pport());
        System.out.println("*****节点地址******"+blockCache.getAddress());

    }


}
