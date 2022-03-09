package com.example.demo.utils;

import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.util.ObjectUtils;

import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description: 文件创建
 * @modifiedBy:
 */
@Log4j2
public class FileUtil{
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
     * @param zipFilePath 压缩输出路径， 为null,则默认：当前工程目录下的 /data/errorPicture/权限下载失败照片.zip
     * @return zip文件 输出路径
     */
    public String toZip(List<File> fileList, String zipFilePath){
        if(ObjectUtils.isEmpty(zipFilePath) || !zipFilePath.endsWith(".zip")){
            zipFilePath= new File("").getAbsolutePath() + "/data/errorPicture/权限下载失败照片.zip";
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

    /**
     *  将 imagePathDir 目录下的图片文件进行压缩， 并非覆盖当前路径的文件，当读取文件出错是，将图片复制到 errPathDir 目录下后删除原路径图片，
     *  imagePathDir 默认文件路径： /data/originalPhoto
     *  errPathDir 默认文件路径： /data/originalPhoto/errPicture/
     * @param imagePathDir 图片路径文件夹
     * @param errPathDir 读取文件出差时 错误文件保存路径
     */
    public void Thumbnails(String imagePathDir, String errPathDir){
        // 获取 打包成jar 的运行路径
        String path = new ApplicationHome(this.getClass()).getDir().getPath();

        String filePath =path +  "/data/originalPhoto";
        String errorPictureDirPath = "/data/originalPhoto/errPicture/";
        if(imagePathDir != null){
            filePath = imagePathDir;
        }
        if(errPathDir != null){
            errorPictureDirPath = errPathDir;
        }
        File facePhotos = FileUtils.getFile(filePath);
        File errorPictureFileDir = FileUtils.getFile(errorPictureDirPath);
        if(!errorPictureFileDir.exists()){
           errorPictureFileDir.mkdirs();
        }

        if(null != facePhotos && facePhotos.exists()){
            BufferedImage bufferedImage;
            int expectLength = 1000;
            double scale;
            int max;
            String tempPath;
            for (File listFile : Objects.requireNonNull(facePhotos.listFiles())) {
                // 判断文件是否为图片，暂时这么写
                if(isImage(listFile)){
                    try {
                        bufferedImage = Thumbnails.of(listFile).scale(1.0D).asBufferedImage();
                        if(bufferedImage.getWidth() > expectLength ||  bufferedImage.getHeight() > expectLength){
                            max = Math.max(bufferedImage.getWidth(), bufferedImage.getHeight());
                            // 保留1位小数
                            scale = new BigDecimal((float) expectLength/max).setScale(1, RoundingMode.HALF_UP).doubleValue();
                            Thumbnails.of(listFile).scale(scale).outputQuality(1.0D).toFile(filePath);
                        }
                    }catch (Exception e){
//                        e.printStackTrace();
                        tempPath = errorPictureDirPath + listFile.getName();
                        String message = "读取图片异常：图片路径：" + listFile.getPath() + ": 异常信息：" + e.getMessage();
//                        log.err(message);
                        write(errorPictureDirPath + "errMsg.txt", message);
                        // 将照片移动到 errPicture 文件夹下
                        try {
                            FileUtils.copyFileToDirectory(listFile, new File(tempPath));
                            FileUtils.delete(listFile);
                        } catch (IOException ioException) {
                            System.out.println("将读取失败的图片复制到 /errPicture 文件夹下出错；错误信息：" + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private static final HashSet<String> imageSet = new HashSet<>(Arrays.asList("image", "png", "tif", "jpg", "jpeg", "bmp"));

    /**
     * 暂时通过文件名后缀来 判读文件是否为图片
     * @param file file
     * @return boolean
     */
    public static boolean isImage(File file){
        String fileName = file.getName();
        int dot_pos = fileName.lastIndexOf(".");
        if(dot_pos < 0) return false;

        String fileNameExt = fileName.substring(dot_pos + 1);
        if(fileNameExt.length() == 0) return false;

        return imageSet.contains(fileNameExt);
    }
}
