package org.metavm.util.profile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProfileUtils {

    public static void doWithProfile(String name, Runnable action) {
        var s = System.currentTimeMillis();
        action.run();
        log.trace("{} took {} ms", name, System.currentTimeMillis() - s);
    }

}
