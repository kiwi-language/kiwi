package org.metavm.object.type;

import org.metavm.api.JsonIgnore;
import org.metavm.entity.GenericDeclaration;
import org.metavm.util.Utils;

import java.util.List;
import java.util.function.Consumer;

public interface KlassDeclaration extends GenericDeclaration {

    @JsonIgnore
    String getTypeDesc();

    @JsonIgnore
    boolean isConstantPoolParameterized();

    void foreachGenericDeclaration(Consumer<GenericDeclaration> action);

    List<Klass> getKlasses();

    void addKlass(Klass klass);

    default Klass getKlassByByName(String name) {
        return Utils.findRequired(getKlasses(), k -> k.getName().equals(name),
                () -> "Cannot find class '" + name + "' in " + this);
    }

}
