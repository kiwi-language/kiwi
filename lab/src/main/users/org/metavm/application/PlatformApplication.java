package org.metavm.application;

import org.metavm.api.Entity;

@Entity(searchable = true)
public class PlatformApplication extends LabApplication {

    private final static PlatformApplication INSTANCE = new PlatformApplication();

    private PlatformApplication() {
        super("platform");
    }

    public static PlatformApplication getInstance() {
        return INSTANCE;
    }
}
