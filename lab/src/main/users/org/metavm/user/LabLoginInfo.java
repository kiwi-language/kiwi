package org.metavm.user;


import org.metavm.application.LabApplication;
import org.metavm.entity.ValueStruct;

import javax.annotation.Nullable;

@ValueStruct
public record LabLoginInfo(@Nullable LabApplication application, @Nullable LabUser user) {

    public static LabLoginInfo failed() {
        return new LabLoginInfo(null, null);
    }

}
