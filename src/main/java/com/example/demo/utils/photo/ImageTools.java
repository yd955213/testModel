package com.example.demo.utils.photo;

import com.example.demo.entity.api.DownloadAuthorityData;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.*;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 *  * @description:  照片工具类， 包含 图片和base64互转，图片旋转、裁剪,
 * @modifiedBy:
 */
@Log4j2
public class ImageTools {

    /**
     * 获取文件目录下的所有文件路径
     * @param directoryPath
     * @return
     */
    public List<String> getAllFilePathsInDirectory(String directoryPath) throws FileNotFoundException {
        File files = ResourceUtils.getFile(directoryPath);
        final List<String> fileList = new ArrayList<>();
        if(files.exists()){
            Arrays.stream(Objects.requireNonNull(files.listFiles())).forEach(file -> fileList.add(file.getPath()));
        }
        return fileList;
    }

    public String imageToBase64(String filePath){
        File file;
        String imageBase64 = null;
        try {
            file = ResourceUtils.getFile(filePath);
            if(file.exists())
                imageBase64 = imageToBase64(file);
        } catch (FileNotFoundException e) {
            log.info("照片转base64,传入路径错误,文件未找到：{}", filePath );
        }
        return imageBase64;
    }

    private synchronized String imageToBase64(File file){
        long length = file.length();
        byte[] imageBytes = new byte[(int) length];
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        String imageBase64 = null;
        try {
            fileInputStream = new FileInputStream(file);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(imageBytes);
            imageBase64 = Base64.getEncoder().encodeToString(imageBytes).replaceAll("[\\s*\t\n\r]", "");
        } catch (IOException e) {
            log.info("照片转base64失败，错误信息：{}", e.getMessage() );
        }finally {
            if ( null != bufferedInputStream) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {}
            }

            if(null != fileInputStream) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {}
            }
        }
        return imageBase64;
    }
}
