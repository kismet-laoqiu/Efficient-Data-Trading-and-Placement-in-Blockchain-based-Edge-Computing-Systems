package com.example.demo.cpmu.coefficient;

import java.text.DecimalFormat;
import java.util.Random;

public class test {
//    public static void main(String[] args) {
//        float[][] results = new float[16][24];
//        int[] dataSize = new int[]{2, 5, 9, 7, 8, 6, 4, 3, 8, 1, 7, 6, 3, 1, 5, 7, 2};
//
//
//        float min = 0.01f;
//        float max = 0.08f;
//        float floatBounded = min + new Random().nextFloat() * (max - min);
//        for(int i=0;i<16;++i) {
//            System.out.print("[");
//            for (int j=0;j<24;++j) {
//                float random = min + new Random().nextFloat() * (max - min);
//                results[i][j] = dataSize[i]*random;
//                DecimalFormat df = new DecimalFormat("0.00");
//                System.out.print(df.format(results[i][j])+" ");
//            }
//            System.out.println("]");
//        }
//    }

//public static void main(String[] args) {
//    int[] NodeCapacity = new int[24];
//    int min = 15;
//    int max = 25;
//    for (int i=0;i<24;++i) {
//       int  random = min + ((int) (new Random().nextFloat() * (max - min)));
//        System.out.print(random+" ");
//    }
//
//}

//    public static void main(String[] args) {
//        int[][] a = new int[16][24];
//        int[] producer = new int[]{4, 9, 13, 15, 18, 22};
//
//                for(int i=0;i<16;++i) {
//            System.out.print("[");
//            for (int j=0;j<24;++j) {
//
//                a[i][j] = 0;
//
//                System.out.print(a[i][j]+" ");
//            }
//            System.out.println("]");
//        }
//
//
//    }

    public static void main(String[] args) {
    int[] dataRate = new int[16];
    int min = 5;
    int max = 25;
    for (int i=0;i<16;++i) {
       int  random = min + ((int) (new Random().nextFloat() * (max - min)));
        System.out.print(random+" ");
    }

}
}
