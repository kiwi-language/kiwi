package org.manul.object.type;

import org.manul.api.Entity;
import org.manul.api.JsonIgnore;
import org.manul.entity.GenericDeclaration;
import org.manul.util.Utils;

import java.util.List;
import java.util.function.Consumer;

@Entity
public interface KlassDeclaration extends GenericDeclaration {

    @JsonIgnore
    String getTypeDesc();

    @JsonIgnore
    boolean isConstantPoolParameterized();

    void foreachGenericDeclaration(Consumer<GenericDeclaration> action);

    List<Klass> getKlasses();

    void addKlass(Klass klass);

    void removeKlass(Klass klass);

    default Klass getKlassByByName(String name) {
        return Utils.findRequired(getKlasses(), k -> k.getName().equals(name),
                () -> "Cannot find class '" + name + "' in " + this);
    }

}
