package org.itkoukou.oss.ossload;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@EnableFileStorage
@SpringBootApplication
public class OssLoadApplication {

    public static void main(String[] args) {
        SpringApplication.run(OssLoadApplication.class, args);
        System.err.println("SUCCESS");
    }

}
