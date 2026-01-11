package org.manul.entity;

import org.manul.flow.Flow;
import org.manul.object.instance.core.Id;
import org.manul.object.type.ConstantPool;
import org.manul.object.type.TypeVariable;

import javax.annotation.Nullable;
import java.util.List;

public enum DummyGenericDeclaration implements GenericDeclaration {

    INSTANCE

    ;

    @Override
    public List<TypeVariable> getTypeParameters() {
        return List.of();
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
    }

    @Override
    public String getQualifiedName() {
        return "DUMMY";
    }

    @Override
    public String getName() {
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

    @Override
    public GenericDeclarationRef getRef() {
        throw new NullPointerException();
    }

    @Override
    public ConstantPool getConstantPool() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Id tryGetId() {
        return null;
    }
}
