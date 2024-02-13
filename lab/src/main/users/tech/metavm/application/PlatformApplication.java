package tech.metavm.application;

import tech.metavm.entity.EntityType;

@EntityType("平台应用")
public class PlatformApplication extends LabApplication {

    public final static PlatformApplication INSTANCE = new PlatformApplication();

    private PlatformApplication() {
        super("平台应用");
    }

}
