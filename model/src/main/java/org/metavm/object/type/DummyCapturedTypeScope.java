package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.flow.Flow;

import javax.annotation.Nullable;
import java.util.List;

@Entity
public enum DummyCapturedTypeScope implements CapturedTypeScope {

    INSTANCE;

    public String getStringId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CapturedTypeVariable> getCapturedTypeVariables() {
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

    @Override
    public ConstantPool getConstantPool() {
        throw new UnsupportedOperationException();
    }
}
