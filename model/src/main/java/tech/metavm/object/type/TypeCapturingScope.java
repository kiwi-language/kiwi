package tech.metavm.object.type;

import tech.metavm.flow.Flow;

import javax.annotation.Nullable;

public interface TypeCapturingScope {

    String getStringId();

    int getCapturedTypeIndex(CapturedType capturedType);

    String getInternalName(@Nullable Flow current);

    void addCapturedType(CapturedType capturedType);

}
