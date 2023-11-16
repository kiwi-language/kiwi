package tech.metavm.entity;

import tech.metavm.common.RefDTO;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeVariable;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public interface GenericDeclaration {

    List<TypeVariable> getTypeParameters();

    default int getTypeParameterIndex(TypeVariable typeVariable) {
        var index = getTypeParameters().indexOf(typeVariable);
        if(index < 0)
            throw new InternalException(
                    String.format("Type parameter '%s' doesn't exist in generic declaration: %s",
                            typeVariable, this));
        return index;
    }

    @Nullable
    GenericDeclaration getTemplate();

    void addTypeParameter(TypeVariable typeParameter);

    String getKey(Function<Type, java.lang.reflect.Type> getJavaType);

    String getName();

    @Nullable String getCode();

    RefDTO getRef();

}
