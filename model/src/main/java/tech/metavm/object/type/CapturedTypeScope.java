package tech.metavm.object.type;

import tech.metavm.flow.Flow;

import javax.annotation.Nullable;
import java.util.Collection;

public interface CapturedTypeScope {

    String getStringId();

    Collection<CapturedTypeVariable> getCapturedTypeVariables();

    int getCapturedTypeVariableIndex(CapturedTypeVariable capturedTypeVariable);

    String getInternalName(@Nullable Flow current);

    void addCapturedTypeVariable(CapturedTypeVariable capturedTypeVariable);

    String getScopeName();

}
