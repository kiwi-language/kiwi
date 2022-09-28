package tech.metavm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ObjectApplication {

    public static void main(String[] args) {
        System.setProperty("druid.wall.multiStatementAllow", "true");
        SpringApplication.run(ObjectApplication.class);
    }

}
