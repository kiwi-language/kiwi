package tech.metavm.object.type;

import tech.metavm.flow.Flow;

import javax.annotation.Nullable;
import java.util.Collection;

public interface CapturedTypeScope {

    String getStringId();

    Collection<CapturedType> getCapturedTypes();

    int getCapturedTypeIndex(CapturedType capturedType);

    String getInternalName(@Nullable Flow current);

    void addCapturedType(CapturedType capturedType);

    String getScopeName();

}
