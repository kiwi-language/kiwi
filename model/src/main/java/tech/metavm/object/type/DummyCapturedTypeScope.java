package tech.metavm.object.type;

import tech.metavm.entity.EntityType;
import tech.metavm.flow.Flow;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

@EntityType("DummyCapturedTypeScope")
public enum DummyCapturedTypeScope implements CapturedTypeScope {

    INSTANCE;

    public String getStringId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<CapturedType> getCapturedTypes() {
        return List.of();
    }

    @Override
    public int getCapturedTypeIndex(CapturedType capturedType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addCapturedType(CapturedType capturedType) {
    }

    @Override
    public String getScopeName() {
        return "Dummy";
    }
}
