package com.example.demo.blockchain.util;




import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.blockchain.block.Block;
import com.example.demo.blockchain.util.test.Question;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.jdom.Attribute;
import org.jdom.Element;



public class CsvUtils {
        /**
         * 产生Excel试卷
         *@param list 题目集合
         */
        public  void exportExcelPaper(List<Block> list) {
            // 标题
            String[] title = {"high","timeStamp", "nonce",  "putBlockAddress", "hash", "prevBlockHash","merkleHash"};
            // 创建一个工作簿
            HSSFWorkbook workbook = new HSSFWorkbook();
            // 创建一个工作表sheet
            HSSFSheet sheet = workbook.createSheet();
            // 设置列宽
            setColumnWidth(sheet, title.length);
            // 创建第一行
            HSSFRow row = sheet.createRow(0);
            // 创建一个单元格
            HSSFCell cell = null;
            // 创建表头
            for (int i = 0; i < title.length; i++) {
                cell = row.createCell(i);
                // 设置样式
                HSSFCellStyle cellStyle = workbook.createCellStyle();
                //cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 设置字体居中
                // 设置字体
                HSSFFont font = workbook.createFont();
                font.setFontName("宋体");
                //font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 字体加粗
                // font.setFontHeight((short)12);
                font.setFontHeightInPoints((short) 13);
                cellStyle.setFont(font);
                cell.setCellStyle(cellStyle);

                cell.setCellValue(title[i]);
            }


            //System.out.println(list);
            // 从第二行开始追加数据
            for (int i = 1; i < (list.size() + 1); i++) {
                // 创建第i行
                HSSFRow nextRow = sheet.createRow(i);
                for (int j = 0; j < title.length; j++) {
                    //Question eQuestion = list.get(i-1);
                    Block block = list.get(i-1);
                    HSSFCell cell2 = nextRow.createCell(j);
                    //String[] title = {"high","timeStamp", "nonce",  "putBlockAddress", "hash", "prevBlockHash","merkleHash"};
                    if (j == 0) {
                        cell2.setCellValue( block.getHigh());
                    }
                    if (j == 1) {
                        cell2.setCellValue(block.getTimeStamp());
                    }
                    if (j == 2) {
                        cell2.setCellValue(block.getNonce());
                    }
                    if (j == 3) {
                        cell2.setCellValue(block.getPutBlockAddress());
                    }
                    if (j == 4) {
                        cell2.setCellValue(block.getHash());
                    }
                    if (j == 5) {
                        cell2.setCellValue(block.getPrevBlockHash());
                    }
                    if (j == 6) {
                        cell2.setCellValue(block.getMerkleHash());
                    }

                }
            }

            // 创建一个文件
            File file = new File("C:\\Users\\15192\\Desktop\\区块链开源\\demo8 - podt\\data\\podt220-105.xls");
            try {
                file.createNewFile();
                // 打开文件流
                FileOutputStream outputStream = FileUtils.openOutputStream(file);
                workbook.write(outputStream);
                outputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        // 设置列宽()
        private  void setColumnWidth(HSSFSheet sheet, int colNum) {
            for (int i = 0; i < colNum; i++) {
                int v = 0;
                v = Math.round(Float.parseFloat("15.0") * 37F);
                v = Math.round(Float.parseFloat("20.0") * 267.5F);
                sheet.setColumnWidth(i, v);
            }
        }




}
