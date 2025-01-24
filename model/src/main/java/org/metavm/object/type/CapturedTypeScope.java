package org.metavm.object.type;

import org.metavm.flow.Flow;

import javax.annotation.Nullable;
import java.util.List;

public interface CapturedTypeScope {

    String getStringId();

    List<CapturedTypeVariable> getCapturedTypeVariables();

    int getCapturedTypeVariableIndex(CapturedTypeVariable capturedTypeVariable);

    String getInternalName(@Nullable Flow current);

    void addCapturedTypeVariable(CapturedTypeVariable capturedTypeVariable);

    String getScopeName();

    ConstantPool getConstantPool();
}
