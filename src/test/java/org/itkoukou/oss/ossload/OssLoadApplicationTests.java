package org.itkoukou.oss.ossload;

import cn.hutool.core.date.DateUtil;
import jakarta.annotation.Resource;
import org.dromara.x.file.storage.core.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

class OssLoadApplicationTests {
    @Test
    void contextLoads() {
        String format = String.format("%s/", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        System.err.println(format);
    }

}
