package org.metavm.entity;

import org.jetbrains.annotations.Nullable;
import org.metavm.flow.Flow;
import org.metavm.object.type.TypeVariable;

import java.util.List;

@EntityType
public enum DummyGenericDeclaration implements GenericDeclaration {

    INSTANCE

    ;

    @Override
    public List<TypeVariable> getTypeParameters() {
        return List.of();
    }

    @Nullable
    public GenericDeclaration getTemplate() {
        return null;
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
    }

    @Override
    public String getName() {
        return "DUMMY";
    }

    @Override
    public String getCode() {
        return "DUMMY";
    }

    @Override
    public String getTypeDesc() {
        return "DUMMY";
    }

    @Override
    public String getStringId() {
        return null;
    }

    @Override
    public String getInternalName(@javax.annotation.Nullable Flow current) {
        throw new UnsupportedOperationException();
    }
}
