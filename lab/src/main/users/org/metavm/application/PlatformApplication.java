package org.metavm.application;

import org.metavm.entity.EntityType;

@EntityType
public class PlatformApplication extends LabApplication {

    private final static PlatformApplication INSTANCE = new PlatformApplication();

    private PlatformApplication() {
        super("platform");
    }

    public static PlatformApplication getInstance() {
        return INSTANCE;
    }
}
