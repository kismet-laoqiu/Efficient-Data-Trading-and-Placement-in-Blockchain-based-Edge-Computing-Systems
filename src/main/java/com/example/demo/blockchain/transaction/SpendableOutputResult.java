package com.example.demo.blockchain.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 查询结果
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpendableOutputResult {

    /**
     * 交易时的支付金额
     */
    private int accumulated;
    /**
     * 未花费的交易
     */
    private Map<String, int[]> unspentOuts;

}
