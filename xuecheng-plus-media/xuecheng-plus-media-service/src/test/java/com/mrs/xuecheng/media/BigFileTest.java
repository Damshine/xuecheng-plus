package com.mrs.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * description: BigFileTest
 * date: 2023/5/4 20:31
 * author: MR.孙
 */
public class BigFileTest {


    /**
     * 文件分块
     */
    @Test
    void testChunk() throws IOException {

        //源文件
        File sourceFile = new File("D:\\下载文件\\商城项目.mp4");
        //分块文件存储路径
        String chunkFilePath = "D:\\下载文件\\分块\\";

        //分块文件大小
        int chunkSize = 1024 * 1024 * 1;
        //分块文件数量
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);

        //读取源文件数据，向分块文件中写数据
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        byte[] bytes = new byte[1024];
        //分快文件写入流
        for (int i = 0; i < chunkNum; i++) {
            //分块文件  chunkFilePath + 分快数量索引
            File chunkFile = new File(chunkFilePath + i);
            if (chunkFile.exists()) {
                chunkFile.delete();
            }
            boolean newFile = chunkFile.createNewFile();
            if (newFile) {
                RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
                int len = -1;
                while ((len = raf_r.read(bytes)) != -1) {
                    raf_rw.write(bytes, 0, len);
                    if (chunkFile.length() >= chunkSize) {
                        break;
                    }
                }
                raf_rw.close();
                System.out.println("完成分块");
            }

        }
        raf_r.close();
    }

    /***
     * 文件分块合并
     */
    @Test
    void testMerge() throws IOException {

        //分块目录
        File chunkFolder = new File("D:\\下载文件\\分块\\");

        //源文件
        File sourceFile = new File("D:\\下载文件\\商城项目.mp4");
        //合并后的文件
        File mergeFile = new File("D:\\下载文件\\商城项目_2.mp4");

        //获取分块目录下所有分块
        File[] files = chunkFolder.listFiles();

        //因为是分块，需要注意顺序
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, (file1, file2) -> Integer.parseInt(file1.getName()) - Integer.parseInt(file2.getName()));

        //向要合并的文件写入流
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        //缓冲区
        byte[] bytes = new byte[1024];
        //遍历所有分块文件， 向要合并的文件写
        fileList.forEach(file -> {
            //读
            try {
                RandomAccessFile raf_r = new RandomAccessFile(file, "r");
                int len = -1;
                while ((len = raf_r.read(bytes)) != -1) {
                    raf_rw.write(bytes, 0, len);
                }
                raf_r.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        raf_rw.close();

        //合并后的文件和源文件进行校验
        FileInputStream fileInputStream_source = new FileInputStream(sourceFile);
        FileInputStream fileInputStream_merge = new FileInputStream(mergeFile);
        String md5_source = DigestUtils.md5Hex(fileInputStream_source);
        String md5_merge = DigestUtils.md5Hex(fileInputStream_merge);
        if (md5_source.equals(md5_merge)) {
            System.out.println("文件合并成功");
        }
    }


    @Test
    void testTempFile() throws IOException {
        System.out.println(System.getProperty("java.io.tmpdir"));
    }

}





