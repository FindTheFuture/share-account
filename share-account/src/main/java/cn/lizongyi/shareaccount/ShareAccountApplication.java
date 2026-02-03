package cn.lizongyi.shareaccount;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan({"cn.lizongyi.shareaccount.dao"})
@SpringBootApplication(scanBasePackages = {"cn.lizongyi.*"})
@EnableScheduling
@EnableAsync
public class ShareAccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShareAccountApplication.class, args);
    }

}
