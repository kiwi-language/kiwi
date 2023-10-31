package tech.metavm.entity;

import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeVariable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public interface GenericDeclaration {

    List<TypeVariable> getTypeParameters();

    void addTypeParameter(TypeVariable typeParameter);

    String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType);

    String getName();

    @Nullable String getCode();

}
