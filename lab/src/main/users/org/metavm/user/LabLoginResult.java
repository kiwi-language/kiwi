package org.metavm.user;

import org.metavm.entity.ValueStruct;

import javax.annotation.Nullable;

@ValueStruct
public record LabLoginResult(@Nullable String token, LabUser user) {

}
