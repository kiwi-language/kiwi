package tech.metavm.object.type;

import tech.metavm.entity.EntityType;
import tech.metavm.flow.Flow;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

@EntityType
public enum DummyCapturedTypeScope implements CapturedTypeScope {

    INSTANCE;

    public String getStringId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<CapturedTypeVariable> getCapturedTypeVariables() {
        return List.of();
    }

    @Override
    public int getCapturedTypeVariableIndex(CapturedTypeVariable capturedTypeVariable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addCapturedTypeVariable(CapturedTypeVariable capturedTypeVariable) {
    }

    @Override
    public String getScopeName() {
        return "Dummy";
    }
}
