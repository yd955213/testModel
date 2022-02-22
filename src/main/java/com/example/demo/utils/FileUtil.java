package com.example.demo.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description: 文件创建
 * @modifiedBy:
 */
@Log4j2
public class FileUtil {
    private String filePath;
    private File file;
    public FileUtil() {}
    public FileUtil(String filePath) {
        this.filePath = filePath;
        creatFile(filePath);
    }

    public boolean creatFile(String filePath){
        this.filePath = filePath;
        file = new File(filePath);
        boolean create = false;
        if(!file.getParentFile().exists()){
            create = file.getParentFile().mkdir();
        }
        if(!file.exists()){
            try {
                create = file.createNewFile();
            } catch (IOException e) {
                log.error("文件创建失败，预创建文件路径：{}", filePath);
            }
        }
        return create;
    }

    public void write(String msg){
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            if(file != null){
                fileOutputStream = new FileOutputStream(file, true);
                bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                byte[] bytes = (msg + "\r\n" + "\r\n").getBytes(StandardCharsets.UTF_8);
                bufferedOutputStream.write(bytes, 0, bytes.length);
                bufferedOutputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(bufferedOutputStream != null){
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fileOutputStream != null){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void write(String filePath, String msg){
        if(file == null){
            creatFile(filePath);
        }
        write(msg);
    }

    /**
     * 将文件夹路径下的 文件打包压缩为zip文件
     * @param filePathInDirectory 需要打包压缩的文件夹
     * @param zipFilePath zip文件 输出路径，为null,则默认：当前工程目录下的/data/权限下载失败照片.zip
     * @return zip文件 输出路径
     */
    public String toZip(String filePathInDirectory, String zipFilePath){
        File files = new File(filePathInDirectory);
        if(file.exists()){
            File[] fileList = files.listFiles();
            if(!ObjectUtils.isEmpty(fileList))
                toZip(Arrays.asList(fileList), zipFilePath);
        }else {
            log.info("文件压缩成zip失败；文件夹不存在，文件路径：" + filePathInDirectory);
        }
        return zipFilePath;
    }
    /**
     * 将传入的文件列表压缩成zip文件
     * @param fileList 文件list
     * @param zipFilePath 压缩输出路径， 为null,则默认：当前工程目录下的/data/权限下载失败照片.zip
     * @return zip文件 输出路径
     */
    public String toZip(List<File> fileList, String zipFilePath){
        if(ObjectUtils.isEmpty(zipFilePath) || !zipFilePath.endsWith(".zip")){
            zipFilePath= new File("").getAbsolutePath() + "/data/权限下载失败照片.zip";
        }

        FileOutputStream fileOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        BufferedInputStream bufferedInputStream = null;
        FileInputStream fileInputStream = null;
        byte[] bytes;
        ZipEntry zipEntry;
        int length = 1024;
        try {
            fileOutputStream = new FileOutputStream(zipFilePath);
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            for(File file : fileList){
                bytes = new byte[length];
                try {
                    zipEntry = new ZipEntry(file.getName());
                    zipOutputStream.putNextEntry(zipEntry);
                    fileInputStream = new FileInputStream(file);
                    bufferedInputStream = new BufferedInputStream(fileInputStream, length);
                    while (bufferedInputStream.read(bytes) != -1){
                        zipOutputStream.write(bytes, 0, bytes.length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            zipOutputStream.flush();

            log.info("文件压缩成zip成功；zip文件路径：" + zipFilePath);
        }catch (Exception e){
//            e.printStackTrace();
            log.error("文件压缩失败！,失败信息：{}", e.getMessage());
        }finally {
            // Duplicated code fragment (14 lines long)
            if(bufferedInputStream != null){
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fileInputStream != null){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(zipOutputStream != null){
                try {
                    zipOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fileOutputStream != null){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return zipFilePath;
    }
}
