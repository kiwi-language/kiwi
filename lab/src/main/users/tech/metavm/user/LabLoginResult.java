package tech.metavm.user;

import tech.metavm.entity.ValueStruct;

import javax.annotation.Nullable;

@ValueStruct
public record LabLoginResult(@Nullable String token, LabUser user) {

}
