package org.metavm.entity;

import org.metavm.api.JsonIgnore;
import org.metavm.flow.Flow;
import org.metavm.object.type.TypeVariable;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.List;

public interface GenericDeclaration extends Identifiable {

    List<TypeVariable> getTypeParameters();

    default int getTypeParameterIndex(TypeVariable typeVariable) {
        var index = getTypeParameters().indexOf(typeVariable);
        if(index < 0)
            throw new InternalException(
                    String.format("Type parameter '%s' doesn't exist in generic declaration: %s",
                            typeVariable, this));
        return index;
    }

    default TypeVariable getTypeParameterByName(String name) {
        return Utils.findRequired(getTypeParameters(), t -> t.getName().equals(name));
    }

    void addTypeParameter(TypeVariable typeParameter);

    String getQualifiedName();

    String getName();

    String getTypeDesc();

    String getStringId();

    String getInternalName(@Nullable Flow current);

    @JsonIgnore
    GenericDeclarationRef getRef();
}
