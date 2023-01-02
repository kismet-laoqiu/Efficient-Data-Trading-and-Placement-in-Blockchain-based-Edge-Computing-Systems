package com.example.demo.blockchain.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReadFile {
    public static  List<String> getContent(String fileName)  {
        //String fileName = "C:\\Users\\15192\\Desktop\\IPFShash.txt";
        //读取文件
        List<String> lineLists = null;
        try {
            System.out.println(fileName);
            lineLists = Files
                    .lines(Paths.get(fileName), Charset.defaultCharset())
                    .flatMap(line -> Arrays.stream(line.split("\n")))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  lineLists;
    }

    public static void main(String[] args) {
        List<String> list = ReadFile.getContent("C:\\Users\\15192\\Desktop\\IPFShash.txt");
        for (String s : list) {
            System.out.println(s);
        }
    }

}
