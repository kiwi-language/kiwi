package tech.metavm.entity;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeVariable;

import java.util.List;
import java.util.function.Function;

@EntityType("DummyGenericDeclaration")
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
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return "DummyGenericDeclaration";
    }

    @Override
    public String getName() {
        return "DUMMY";
    }

    @Nullable
    @Override
    public String getCode() {
        return "DUMMY";
    }
}
