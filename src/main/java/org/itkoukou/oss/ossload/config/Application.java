package org.itkoukou.oss.ossload.config;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import jakarta.annotation.Resource;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.platform.MinioFileStorage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.Map;
import static cn.hutool.core.util.IdUtil.getSnowflakeNextId;

@RestController
public class Application {

    @Resource
    private FileStorageService x_file;//注入实列
    public  MinioClient minioClient ;//注入实列
    //获取时间列表
    public static final String FILEPATH = String.format("%s/", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd")));

    /**
     *上传压缩包
     */
    @PostMapping(value = "/uploadZip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Map<String, String>> uploadZip(@RequestPart("file") MultipartFile file) throws Exception {
        String s = uploadFolder("D:\\data\\大雁塔");
        return R.ok(s);
    }
    /**
     * 上传普通附件
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FileInfo> upload(@RequestPart("file") MultipartFile file)  {
        return R.ok(x_file.of(file)
                .setPath(FILEPATH)
                .setObjectId(getSnowflakeNextId())
                .upload());
    }

    /**
     * @param filePath 文件夹路径
     */
    private String uploadFolder(String filePath) throws Exception {
        String format = String.format("%s/", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        File file = new File(filePath);
        String rootName = file.getName();
         minioClient = ((MinioFileStorage) x_file.getFileStorage()).getClient();
        if (!file.isDirectory()) {
            upload(file, rootName  , format , minioClient);
        } else {
            recursion(file, rootName , format , minioClient);
        }
        return "";
    }

    public void recursion(File file, String rootName , String format  , MinioClient client) throws Exception {
        File[] files = file.listFiles();
        if (files != null) {
            for (File currFile : files) {
                if (currFile.isDirectory()) {
                    recursion(currFile, rootName , format , client );
                } else {
                    upload(currFile, rootName  , format ,client);
                }
            }
        }
    }
    public  void upload(File file, String rootName  , String format , MinioClient client ) throws Exception {
        String rootPath = file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(rootName)).replace("\\", "/");
        client.uploadObject(UploadObjectArgs.builder()
                .bucket(((MinioFileStorage) x_file.getFileStorage()).getBucketName())
                .object(format + "/" + rootPath)
                .filename(file.getAbsolutePath())
                .build()
        );

    }
}
