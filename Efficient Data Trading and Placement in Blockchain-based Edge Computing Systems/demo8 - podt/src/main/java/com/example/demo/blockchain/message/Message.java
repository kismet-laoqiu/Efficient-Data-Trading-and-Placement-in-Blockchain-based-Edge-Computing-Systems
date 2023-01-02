package com.example.demo.blockchain.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {


    /**
     * 消息类型
     */
    private int type;
    /**
     * 消息内容
     */
    private String data;

    public Message(int type) {
        this.type = type;
    }
}