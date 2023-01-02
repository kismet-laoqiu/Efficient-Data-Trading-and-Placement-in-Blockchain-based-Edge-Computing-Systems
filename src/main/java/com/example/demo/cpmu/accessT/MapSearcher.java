package com.example.demo.cpmu.accessT;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * 迪杰斯特拉(Dijkstra)图最短路径搜索算法
 * <br/>每次开始新的搜索需要创建此类对象
 * @param <T> 节点的主键类型
 * @author qkm
 * @date 2014-7-26-下午9:45:07
 */
public class MapSearcher<T> {

    /**
     * 最短路径搜索结果类
     * @author qkm
     * @date 2014-7-27-下午3:55:11
     * @param <T> 节点的主键类型
     */
    public static class SearchResult<T> {
        /**
         * 最短路径结果
         */
        List<T> path;
        /**
         * 最短距离
         */
        Integer distance;

        /**
         * 获取实例
         * @param path 最短路径结果
         * @param distance 最短路径距离
         * @return
         */
        protected static <T> SearchResult<T> valueOf(List<T> path, Integer distance) {
            SearchResult<T> r = new SearchResult<T>();
            r.path = path;
            r.distance = distance;
            return r;
        }

        public List<T> getPath() {
            return path;
        }
        public Integer getDistance() {
            return distance;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("path:");
            for(Iterator<T> it = this.path.iterator(); it.hasNext();) {
                sb.append(it.next());
                if(it.hasNext()) {
                    sb.append("->");
                }
            }
            sb.append("\n").append("distance:").append(distance);
            return sb.toString();
        }

    }

    /**
     * 地图对象
     */
    Maps<T> map;
    /**
     * 开始节点
     */
    Maps.Node<T> startNode;
    /**
     * 结束节点
     */
    Maps.Node<T> targetNode;
    /**
     * 开放的节点
     */
    Set<Maps.Node<T>> open = new HashSet<Maps.Node<T>>();
    /**
     * 关闭的节点
     */
    Set<Maps.Node<T>> close = new HashSet<Maps.Node<T>>();
    /**
     * 最短路径距离
     */
    Map<Maps.Node<T>, Integer> path = new HashMap<Maps.Node<T>, Integer>();
    /**
     * 最短路径
     */
    Map<T, List<T>> pathInfo = new HashMap<T, List<T>>();

    /**
     * 初始化起始点
     * <br/>初始时，S只包含起点s；U包含除s外的其他顶点，且U中顶点的距离为"起点s到该顶点的距离"
     * [例如，U中顶点v的距离为(s,v)的长度，然后s和v不相邻，则v的距离为∞]。
     * @param source 起始节点的Id
     * @param map 全局地图
     * @param closeSet 已经关闭的节点列表
     * @return
     */
    @SuppressWarnings("unchecked")
    public Maps.Node<T> init(T source, Maps<T> map, Set<T> closeSet) {

        Map<T, Maps.Node<T>> nodeMap = map.getNodes();
        Maps.Node<T> startNode = nodeMap.get(source);
        //将初始节点放到close
        close.add(startNode);
        //将其他节点放到open
        for(Maps.Node<T> node : nodeMap.values()) {
            if(!closeSet.contains(node.getId()) && !node.getId().equals(source)) {
                this.open.add(node);
            }
        }

        // 初始路径
        T startNodeId = startNode.getId();
        for(Entry<Maps.Node<T>, Integer> entry : startNode.getChilds().entrySet()) {
            Maps.Node<T> node = entry.getKey();
            if(open.contains(node)) {
                T nodeId = node.getId();
                path.put(node, entry.getValue());
                pathInfo.put(nodeId, new ArrayList<T>(Arrays.asList(startNodeId, nodeId)));
            }
        }

        for(Maps.Node<T> node : nodeMap.values()) {
            if(open.contains(node) && !path.containsKey(node)) {
                path.put(node, Integer.MAX_VALUE);
                pathInfo.put(node.getId(), new ArrayList<T>(Arrays.asList(startNodeId)));
            }
        }
        this.startNode = startNode;
        this.map = map;
        return startNode;
    }


    /**
     * 递归Dijkstra
     * @param start 已经选取的最近节点
     */
    protected void computePath(Maps.Node<T> start) {
        // 从U中选出"距离最短的顶点k"，并将顶点k加入到S中；同时，从U中移除顶点k。
        Maps.Node<T> nearest = getShortestPath(start);
        if (nearest == null) {
            return;
        }
        //更新U中各个顶点到起点s的距离。
        //之所以更新U中顶点的距离，是由于上一步中确定了k是求出最短路径的顶点，从而可以利用k来更新其它顶点的距离；
        //例如，(s,v)的距离可能大于(s,k)+(k,v)的距离。
        close.add(nearest);
        open.remove(nearest);
        //已经找到结果
        if(nearest == this.targetNode) {
            return;
        }
        Map<Maps.Node<T>, Integer> childs = nearest.getChilds();
        for (Maps.Node<T> child : childs.keySet()) {
            if (open.contains(child)) {// 如果子节点在open中
                Integer newCompute = path.get(nearest) + childs.get(child);
                if (path.get(child) > newCompute) {// 之前设置的距离大于新计算出来的距离
                    path.put(child, newCompute);

                    List<T> path = new ArrayList<T>(pathInfo.get(nearest.getId()));
                    path.add(child.getId());
                    pathInfo.put(child.getId(), path);
                }
            }
        }
// computePath(start);// 重复执行自己,确保所有子节点被遍历
        computePath(nearest);// 向外一层层递归,直至所有顶点被遍历
    }

    /**
     * 获取与node最近的子节点
     */
    private Maps.Node<T> getShortestPath(Maps.Node<T> node) {
        Maps.Node<T> res = null;
        int minDis = Integer.MAX_VALUE;
        for (Maps.Node<T> entry : path.keySet()) {
            if (open.contains(entry)) {
                int distance = path.get(entry);
                if (distance < minDis) {
                    minDis = distance;
                    res = entry;
                }
            }
        }
        return res;
    }

    /**
     * 获取到目标点的最短路径
     *
     * @param target
     *      目标点
     * @return
     */
    public SearchResult<T> getResult(T target) {
        Maps.Node<T> targetNode = this.map.getNodes().get(target);
        if(targetNode == null) {
            throw new RuntimeException("目标节点不存在!");
        }
        this.targetNode = targetNode;
        //开始计算
        this.computePath(startNode);
        return SearchResult.valueOf(pathInfo.get(target), path.get(targetNode));
    }

    /**
     * 打印出所有点的最短路径
     */
    public void printPathInfo() {
        Set<Map.Entry<T, List<T>>> pathInfos = pathInfo.entrySet();
        for (Map.Entry<T, List<T>> pathInfo : pathInfos) {
            System.out.println(pathInfo.getKey() + ":" + pathInfo.getValue());
        }
    }

    private static int getStrCunt(String mainStr, String subStr) {
        // 声明一个要返回的变量
        int count = 0;
        // 声明一个初始的下标，从初始位置开始查找
        int index = 0;
        // 获取主数据的长度
        int mainStrLength = mainStr.length();
        // 获取要查找的数据长度
        int subStrLength = subStr.length();
        // 如果要查找的数据长度大于主数据的长度则返回0
        if (subStrLength > mainStrLength) {return 0;}
        // 循环使用indexOf查找出现的下标，如果出现一次则count++
        while ((index = mainStr.indexOf(subStr, index)) != -1) {

            count++;
            // 从找到的位置下标加上要查找的字符串长度，让指针往后移动继续查找
            index += subStrLength;
        }
        return count;
    }

    private static int[] getStrIndex(String mainStr, String subStr) {
        int[] allIndex= new int[getStrCunt(mainStr,subStr)];
        // 声明一个要返回的变量
        int count = 0;
        // 声明一个初始的下标，从初始位置开始查找
        int index = 0;
        // 获取主数据的长度
        int mainStrLength = mainStr.length();
        // 获取要查找的数据长度
        int subStrLength = subStr.length();
        // 如果要查找的数据长度大于主数据的长度则返回0
        if (subStrLength > mainStrLength) {return allIndex;}
        // 循环使用indexOf查找出现的下标，如果出现一次则count++
        while ((index = mainStr.indexOf(subStr, index)) != -1) {

            allIndex[count] = index;
            count++;
            // 从找到的位置下标加上要查找的字符串长度，让指针往后移动继续查找
            index += subStrLength;
        }
        System.out.println(Arrays.toString(allIndex));
        return allIndex;
    }


    /**
     * 测试方法
     */
    public static void main(String[] args) {

        Maps.MapBuilder<String> mapBuilder = new Maps.MapBuilder<String>().create();
        //构建节点
        for (int i=1;i<26;++i) {
            mapBuilder.addNode(Maps.Node.valueOf(String.valueOf((char)('A'+i-1))));
        }
        String nodePath = "<1,2>,\n" +
                " <2,1>,\n" +
                " <1,6>,\n" +
                " <6,1>,\n" +
                " <2,3>,\n" +
                " <3,2>,\n" +
                " <2,6>,\n" +
                " <6,2>,\n" +
                " <3,4>,\n" +
                " <4,3>,\n" +
                " <3,5>,\n" +
                " <5,3>,\n" +
                " <3,7>,\n" +
                " <7,3>,\n" +
                " <4,5>,\n" +
                " <5,4>,\n" +
                " <4,7>,\n" +
                " <7,4>,\n" +
                " <5,8>,\n" +
                " <8,5>,\n" +
                " <6,7>,\n" +
                " <7,6>,\n" +
                " <6,9>,\n" +
                " <9,6>,\n" +
                " <6,11>,\n" +
                " <11,6>,\n" +
                " <7,8>,\n" +
                " <8,7>,\n" +
                " <7,9>,\n" +
                " <9,7>,\n" +
                " <8,10>,\n" +
                " <10,8>,\n" +
                " <9,10>,\n" +
                " <10,9>,\n" +
                " <9,11>,\n" +
                " <11,9>,\n" +
                " <9,12>,\n" +
                " <12,9>,\n" +
                " <10,13>,\n" +
                " <13,10>,\n" +
                " <10,14>,\n" +
                " <14,10>,\n" +
                " <11,12>,\n" +
                " <12,11>,\n" +
                " <11,15>,\n" +
                " <15,11>,\n" +
                " <11,19>,\n" +
                " <19,11>,\n" +
                " <12,13>,\n" +
                " <13,12>,\n" +
                " <12,16>,\n" +
                " <16,12>,\n" +
                " <13,14>,\n" +
                " <14,13>,\n" +
                " <13,17>,\n" +
                " <17,13>,\n" +
                " <14,18>,\n" +
                " <18,14>,\n" +
                " <15,16>,\n" +
                " <16,15>,\n" +
                " <15,20>,\n" +
                " <20,15>,\n" +
                " <16,17>,\n" +
                " <17,16>,\n" +
                " <16,21>,\n" +
                " <21,16>,\n" +
                " <16,22>,\n" +
                " <22,16>,\n" +
                " <17,18>,\n" +
                " <18,17>,\n" +
                " <17,22>,\n" +
                " <22,17>,\n" +
                " <17,23>,\n" +
                " <23,17>,\n" +
                " <18,24>,\n" +
                " <24,18>,\n" +
                " <19,20>,\n" +
                " <20,19>,\n" +
                " <20,21>,\n" +
                " <21,20>,\n" +
                " <21,22>,\n" +
                " <22,21>,\n" +
                " <22,23>,\n" +
                " <23,22>,\n" +
                " <23,24>,\n" +
                " <24,23> ";
        nodePath = nodePath.replace(",\n","");
        System.out.println(nodePath);
        int count = getStrCunt(nodePath,",");
        System.out.println(count);
        for(int i=24;i>=1;--i) {
            nodePath = nodePath.replace(String.valueOf(i),String.valueOf((char)('A'+i-1)));
        }
        System.out.println(nodePath);
        int[] allIndex = getStrIndex(nodePath,",");



        for (int i=0;i<count;++i) {
            //System.out.println(String.valueOf(nodePath.charAt(allIndex[i])-1)+" "+String.valueOf(nodePath.charAt(allIndex[i])+1));
            //System.out.println(allIndex[i]);
            //System.out.println(nodePath.charAt(allIndex[i]-1));
            mapBuilder.addPath(String.valueOf(nodePath.charAt(allIndex[i]-1)), String.valueOf(nodePath.charAt(allIndex[i]+1)), 1);
        }
//        //构建路径
//        mapBuilder.addPath("A", "B", 1);
//        mapBuilder.addPath("A", "F", 2);
//        mapBuilder.addPath("A", "D", 4);
//        mapBuilder.addPath("A", "C", 1);
//        mapBuilder.addPath("A", "G", 5);
//        mapBuilder.addPath("C", "G", 3);
//        mapBuilder.addPath("G", "H", 1);
//        mapBuilder.addPath("H", "B", 4);
//        mapBuilder.addPath("B", "F", 2);
//        mapBuilder.addPath("E", "F", 1);
//        mapBuilder.addPath("D", "E", 1);
//        mapBuilder.addPath("H", "I", 1);
//        mapBuilder.addPath("C", "I", 1);

        //构建全局Map
        Maps<String> map = mapBuilder.build();

        //创建路径搜索器(每次搜索都需要创建新的MapSearcher)
//        MapSearcher<String> searcher = new MapSearcher<String>();
//        //创建关闭节点集合
//        Set<String> closeNodeIdsSet = new HashSet<String>();
//        closeNodeIdsSet.add("Z");
        float[][] results = new float[24][24];
        int[] producer = new int[]{4, 9, 13, 15, 18, 22};

        for(int i=0;i<24;++i) {
            for(int j=0;j<24;++j) {
                //创建路径搜索器(每次搜索都需要创建新的MapSearcher)
                MapSearcher<String> searcher = new MapSearcher<String>();
                //创建关闭节点集合
                Set<String> closeNodeIdsSet = new HashSet<String>();
                closeNodeIdsSet.add("Z");
                searcher.init(String.valueOf((char)('A'+i)), map, closeNodeIdsSet);
                //获取结果
                SearchResult<String> result = searcher.getResult(String.valueOf((char)('A'+j)));

                if(i==j) {
                    results[i][j]=100;
                }else {
                   results[i][j]= (float) (result.distance);
                }

            }
        }
        float[][] producerResult = new float[24][24];
        for(int i=0;i<24;++i) {
            System.out.print("[");
            for (int j = 0; j < 24; ++j) {
                //producerResult[i][j]=results[i][producer[j]];
                System.out.print(results[i][j]+" ");

            }
            System.out.println("]");
        }

                MapSearcher<String> searcher = new MapSearcher<String>();
        //创建关闭节点集合
        Set<String> closeNodeIdsSet = new HashSet<String>();
        closeNodeIdsSet.add("Z");
        //设置初始节点
        searcher.init("A", map, closeNodeIdsSet);
        //获取结果
        SearchResult<String> result = searcher.getResult("X");
        System.out.println(result.toString());
        System.out.println(JSON.toJSONString(results));
        String resultStr = JSON.toJSONString(results);
        String s = resultStr.replace("[","{").replace("]","}");
        System.out.println(s);
        //test.printPathInfo();
    }

}
