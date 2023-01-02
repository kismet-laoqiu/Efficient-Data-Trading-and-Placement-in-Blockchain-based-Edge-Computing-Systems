package com.example.demo.blockchain.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;

public class SerializeUtils {

    /**
     * 反序列化
     *
     * @param bytes 对象对应的字节数组
     * @return
     */
    public static Object deserialize(byte[] bytes) {
        Input input = new Input(bytes);
        Kryo kryo =  new Kryo();
        Object obj = kryo.readClassAndObject(input);
        input.close();
        return obj;
    }

    /**
     * 序列化
     *
     * @param object 需要序列化的对象
     * @return
     */
    public static byte[] serialize(Object object) {


        ByteArrayOutputStream byteBufferOutputStream = new ByteArrayOutputStream();
//设置输出流大小限制为100000字节
        Output output = new Output(byteBufferOutputStream,1000000);


        //Kryo kryo =  new Kryo().writeClassAndObject(output, object);
        Kryo kryo =  new Kryo();
        //kryo.register(Block.class);
     /*   kryo.setRegistrationRequired(true);
        //kryo.register(HashMap.class, new JavaSerializer());
        kryo.register(Block.class, new JavaSerializer());
        kryo.register(Blockchain.class, new JavaSerializer());
        kryo.register(podt Result.class, new JavaSerializer());
        kryo.register(ProofOfWork.class, new JavaSerializer());
        kryo.register(SerializeUtils.class, new JavaSerializer());
        kryo.register(RocksDBUtils.class, new JavaSerializer());

      */
        kryo.writeClassAndObject(output,object);
        byte[] bytes = output.toBytes();
        output.close();
        return bytes;
    }
}
