package com.debox.reward;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.debox.reward.modules.**.mapper")
@SpringBootApplication
public class DeboxRewardApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeboxRewardApplication.class, args);
    }
}
