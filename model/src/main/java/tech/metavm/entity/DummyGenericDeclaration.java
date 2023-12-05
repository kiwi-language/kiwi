package tech.metavm.entity;

import org.jetbrains.annotations.Nullable;
import tech.metavm.common.RefDTO;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeVariable;

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

    @Nullable
    @Override
    public GenericDeclaration getTemplate() {
        return null;
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
    }

    @Override
    public String getKey(Function<Type, java.lang.reflect.Type> getJavaType) {
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

    @Override
    public RefDTO getRef() {
        return new RefDTO(-1L, 0L);
    }
}
