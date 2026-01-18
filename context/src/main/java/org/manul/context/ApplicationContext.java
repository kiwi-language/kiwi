package org.manul.context;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;

@Slf4j
public class ApplicationContext {

    @Getter
    private static volatile boolean stopped;

    public static void start(String...modules) {
        start0(() -> BeanRegistry.instance.initializeModules(new HashSet<>(Arrays.asList(modules))));
    }

    public static void start(Class<?>...classes) {
        start0(() -> BeanRegistry.instance.initializeClasses(new HashSet<>(Arrays.asList(classes))));
    }

    private static void start0(Runnable initialize) {
        try {
            var start = System.currentTimeMillis();
            initialize.run();
            Runtime.getRuntime().addShutdownHook(new Thread(ApplicationContext::stop));
            var elapsed = System.currentTimeMillis() - start;
            log.info("Application started in {} ms", elapsed);
        } catch (Exception e) {
            stopped = true;
            log.error("Application start failed");
            throw e;
        }

    }

    private static void stop() {
        if (stopped)
            throw new IllegalStateException(("Application is already stopped"));
        log.info("Stopping application...");
        stopped = true;
        BeanRegistry.instance.forEachBean(bean -> {
            if (bean instanceof DisposableBean d)
                d.destroy();
        });
        log.info("Application stopped");
    }

}
