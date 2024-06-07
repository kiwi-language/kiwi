package tech.metavm.application;

import tech.metavm.entity.EntityType;

@EntityType
public class PlatformApplication extends LabApplication {

    private final static PlatformApplication INSTANCE = new PlatformApplication();

    private PlatformApplication() {
        super("平台应用");
    }

    public static PlatformApplication getInstance() {
        return INSTANCE;
    }
}
