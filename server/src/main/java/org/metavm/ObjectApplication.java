package org.metavm;

import org.metavm.springconfig.KiwiConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@EnableAspectJAutoProxy
@EnableWebSocket
public class ObjectApplication {

    public static void main(String[] args) {
        parseArgs(args);
        SpringApplication.run(ObjectApplication.class);
    }

    private static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-config")) {
                if (i >= args.length - 1) {
                    System.err.println("Invalid options");
                    System.exit(1);
                }
                KiwiConfig.CONFIG_PATH = args[++i];
            }
        }
    }

}
