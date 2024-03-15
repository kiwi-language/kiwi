package tech.metavm.entity;

import tech.metavm.flow.Flow;
import tech.metavm.object.type.TypeVariable;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.List;

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

    void addTypeParameter(TypeVariable typeParameter);

    String getName();

    @Nullable String getCode();

    String getStringId();

    String getInternalName(@Nullable Flow current);

}
