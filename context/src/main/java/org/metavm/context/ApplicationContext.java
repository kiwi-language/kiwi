package org.metavm.context;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;

@Slf4j
public class ApplicationContext {

    private static volatile boolean isShutdown;

    public static void start(String...modules) {
        try {
            var start = System.currentTimeMillis();
            BeanRegistry.instance.initialize(new HashSet<>(Arrays.asList(modules)));
            Runtime.getRuntime().addShutdownHook(new Thread(ApplicationContext::shutdown));
            var elapsed = System.currentTimeMillis() - start;
            log.info("Application started in {} ms", elapsed);
        } catch (Exception e) {
            isShutdown = true;
            log.error("Application start failed");
            throw e;
        }
    }

    private static void shutdown() {
        if (!isShutdown())
            throw new IllegalStateException("Application context already shutdown");
        isShutdown = true;
        BeanRegistry.instance.forEachBean(bean -> {
            if (bean instanceof DisposableBean d)
                d.destroy();
        });
    }

    public static boolean isShutdown() {
        return isShutdown;
    }

}
