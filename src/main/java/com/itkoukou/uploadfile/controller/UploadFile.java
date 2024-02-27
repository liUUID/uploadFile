package com.itkoukou.uploadfile.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import io.micrometer.common.util.StringUtils;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
public class UploadFile {
    @Value("${oss.endpoint}")
    private static  String url;
    @Value("${oss.access-key}")
    private static  String user;
    @Value("${oss.secret-key}")
    private static  String pwd;
    @Value("${oss.bucket}")
    private static  String bucket;
    private static  String minioPath;

    private static  String localPath;



    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadModel(@RequestPart("file") MultipartFile file, @RequestParam(required = true)  Integer fileType) throws Exception {
        if (ObjectUtil.isNull(file)) {
            return ("上传文件不能为空");
        }
      String url = "";
        switch (fileType){
            case  0 :
                url = uploadFolder(file); // 普通文件
                break;
            case  1 :
                url = upload(file); // 文件夹
                break;
        }
        return url;
    }

    private String upload(MultipartFile file) {

        return null;
    }

    private String uploadFolder(MultipartFile file) {
        //唯一标识
        String fileIdName = IdUtil.simpleUUID();
        //文件临时目录
        String filePath = localPath;
        //删除临时目录
        FileUtil.del(localPath);
        //获取文件原始名称
        String originalFilename = file.getOriginalFilename();
        //文件后缀
        String fileSuffix = getFileExtension(originalFilename);
        if (!fileSuffix.equals("zip")){
            throw new RuntimeException("文件后缀有误");
        }
        //文件名称
        String fileName = getFileNameWithoutExtension(file);
        //当前实际作为目录结构
        String format = DateUtil.format(DateUtil.parse(DateUtil.today()), "yyyy/MM/dd");
        FileUtil.mkdir(filePath);
        //解压文件到临时目录
        extractZipFile(file ,filePath);
        //上传操作

        //删除临时目录
        FileUtil.del(localPath);
        return null;
    }
    public static void recursion(File file, String rootName) throws Exception {
        File[] files = file.listFiles();
        for (File currFile : files) {
            if (currFile.isDirectory()) {
                recursion(currFile, rootName);
            } else {
                upload(currFile, rootName);
            }
        }
    }
    public static void upload(File file, String rootName) throws Exception {
        String rootPath = file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(rootName)).replace("\\", "/");
        MinioClient minioClient =
        MinioClient.builder().endpoint(url).credentials(user, pwd).build();
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucket)
                        .object(minioPath + rootPath)
                        .filename(file.getAbsolutePath())
                        .build());
    }
    public static String getFileExtension(String filename) {
        if (StringUtils.isNotBlank(filename)) {
            int dotIndex = filename.lastIndexOf(".");
            if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
                return filename.substring(dotIndex + 1);
            }
        }
        return null;
    }
    public static String getFileNameWithoutExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String fileName = new File(originalFilename).getName();
            int lastDotIndex = fileName.lastIndexOf(".");
            if (lastDotIndex != -1) {
                fileName = fileName.substring(0, lastDotIndex);
            }
            return fileName;
        }
        return null;
    }
    public static  boolean extractZipFile(MultipartFile file, String targetFolderPath) {
        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String entryName = entry.getName();
                    File outputFile = new File(targetFolderPath + File.separator + entryName);
                    File parentDir = outputFile.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zipInputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
            return true; // 解压成功
        } catch (IOException e) {
            e.printStackTrace();
            return false; // 解压失败
        }
    }
}
