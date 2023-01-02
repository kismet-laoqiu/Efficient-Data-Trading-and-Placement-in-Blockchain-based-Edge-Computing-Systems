package com.example.demo;

import com.alibaba.fastjson.JSON;
import com.example.demo.blockchain.cli.CLI;
import com.example.demo.blockchain.cli.P2PService;
import com.example.demo.blockchain.message.Message;
import com.example.demo.blockchain.util.BlockConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller
public class BlockController {
	@Resource
	CLI cli;

	@Resource
	P2PService p2PService;

	/**
	 * 查看当前节点区块链数据
	 * @return
	 */
	@GetMapping("/")
	@ResponseBody
	public String testBlock() throws Exception {
//		cli.createWallet();
//		cli.createWallet();
		/**
		 * wallet address : 13ZFHmaYwmqZcHK9KBKE9EZ4hXbBopFxDC
		 * wallet address : 112NGFcNVuGsT527pZe4EsdcW5e1gqMpA5
		 */


//		cli.createBlockchain("13ZFHmaYwmqZcHK9KBKE9EZ4hXbBopFxDC");
//		cli.send("13ZFHmaYwmqZcHK9KBKE9EZ4hXbBopFxDC","112NGFcNVuGsT527pZe4EsdcW5e1gqMpA5",5);

		cli.printChain();
		return "";
	}

	/**
	 * 查看当前节点区块链数据
	 * @return
	 */
	@GetMapping("/scan")
	@ResponseBody
	public String scanBlock() {
		//return cli.FirstBlockToJSON();
		return JSON.toJSONString(cli.blockChainToJSON());

	}

	@GetMapping("/syn")
	@ResponseBody
	public String synBlockChain() {
		//return cli.FirstBlockToJSON();
		Message msg = new Message();
		msg.setType(BlockConstant.RESPONSE_BLOCKCHAIN);
		msg.setData(JSON.toJSONString(cli.blockChainToJSON()));
		p2PService.broatcast(JSON.toJSONString(msg));
		return JSON.toJSONString(cli.blockChainToJSON());

	}


	@GetMapping("/test")
	@ResponseBody
	public void testChain() {
	cli.TransactionAndAddBlock();

	}

	@GetMapping("/sendtest")
	@ResponseBody
	public void sendTest() {
		cli.sendP2PTest();

	}



}
