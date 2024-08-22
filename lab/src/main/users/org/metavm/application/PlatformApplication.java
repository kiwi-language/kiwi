package org.metavm.application;

import org.metavm.api.EntityType;

@EntityType(searchable = true)
public class PlatformApplication extends LabApplication {

    private final static PlatformApplication INSTANCE = new PlatformApplication();

    private PlatformApplication() {
        super("platform");
    }

    public static PlatformApplication getInstance() {
        return INSTANCE;
    }
}
