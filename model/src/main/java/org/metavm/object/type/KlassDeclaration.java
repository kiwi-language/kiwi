package org.metavm.object.type;

import org.metavm.api.JsonIgnore;
import org.metavm.entity.GenericDeclaration;

import java.util.function.Consumer;

public interface KlassDeclaration extends GenericDeclaration {

    @JsonIgnore
    String getTypeDesc();

    @JsonIgnore
    boolean isConstantPoolParameterized();

    void foreachGenericDeclaration(Consumer<GenericDeclaration> action);
}
