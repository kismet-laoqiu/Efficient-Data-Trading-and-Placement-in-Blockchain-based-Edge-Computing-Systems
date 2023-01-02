package com.example.demo.blockchain.wallet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Graph implements Serializable {

    /**
     * 顶点数
     */
    private int vertices;


    private int[][] list = new int[18][18];
    private String[] vertexList = new String[18];


    public Graph() {
        initGraph();
    }

    private void initGraph() {
        this.vertexList = giveVertexList();
        this.list = randomList();
        this.vertices = this.vertexList.length;


    }

    public String[] giveVertexList () {
        Set<String> walletAddresses = new HashSet<>();
        try {
            walletAddresses = WalletUtils.getInstance().getAddresses();
        } catch (Exception e) {
            e.printStackTrace();
        }
        walletAddresses.toArray(vertexList);
        return vertexList;

    }

    public int[][] randomList() {
        int a[][]=new int[18][18];

        for(int i=0;i<a.length;i++)
        {
            for(int j=0;j<a[i].length;j++)
            {
                //a[i][j]=r.nextInt();//产生随机整数，需要在前面加上Random r=new Random()和导入Random类:import java.util.Random;
                a[i][j]=(int)(Math.random()*20+1);//产生1~10的随机整数，(int)表示将double类型强转成整型
                //System.out.print(a[i][j]+"\t");
            }

        }
        return a;

    }

    public int getRip(Graph graph,String walletAddress1,String walletAddress2) {

       int j=0,i=0;
        for(;i<graph.vertices;++i) {
            if(graph.vertexList[i].equals(walletAddress1)) {
                break;
            }

        }
        for(;j<graph.vertices;++j) {
            if(graph.vertexList[j].equals(walletAddress2)) {
                break;
            }
        }
        System.out.println(i+" "+j);



        return graph.list[i][j];

    }


    public String toStr(int[][] list) {
        StringBuffer result = new StringBuffer();
        String separator = ",";
        //float[][] values = new float[50][50];


        for (int i = 0; i < list.length; ++i)
        {
            result.append('[');
            for (int j = 0; j < list[i].length; ++j)
                if (j == list[i].length-1)
                    result.append(list[i][j]);
                else
                    result.append(list[i][j]).append(separator);
            result.append(']');
        }
        return result.toString();
    }

    public void printGraph() {
        System.out.println((this.vertices));
        System.out.println(this.toStr(this.list));
        System.out.println(Arrays.toString(this.vertexList));
    }

    public static void main(String[] args) {
        Graph graph = new Graph();
        System.out.println(Arrays.toString(graph.vertexList));
        System.out.println(graph.toStr(graph.list));
        System.out.println(graph.getRip(graph,"1PWHHe4bQZseuGssEHtEeFayN3jHQpA3P4","1PWHHe4bQZseuGssEHtEeFayN3jHQpA3P4"));

    }



}
