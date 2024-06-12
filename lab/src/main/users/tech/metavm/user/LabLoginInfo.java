package tech.metavm.user;


import tech.metavm.application.LabApplication;
import tech.metavm.entity.ValueStruct;

import javax.annotation.Nullable;

@ValueStruct
public record LabLoginInfo(@Nullable LabApplication application, @Nullable LabUser user) {

    public static LabLoginInfo failed() {
        return new LabLoginInfo(null, null);
    }

}
