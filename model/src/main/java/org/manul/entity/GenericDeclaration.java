package org.manul.entity;

import org.manul.api.Entity;
import org.manul.api.JsonIgnore;
import org.manul.flow.Flow;
import org.manul.object.type.ConstantPool;
import org.manul.object.type.TypeVariable;
import org.manul.util.InternalException;
import org.manul.util.Utils;

import javax.annotation.Nullable;
import java.util.List;

@Entity
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

    ConstantPool getConstantPool();

}
