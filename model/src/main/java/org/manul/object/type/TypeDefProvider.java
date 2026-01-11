package org.manul.object.type;

import org.manul.object.instance.core.Id;

public interface TypeDefProvider {

    ITypeDef getTypeDef(Id id);

    default Klass getKlass(Id id) {
        return (Klass) getTypeDef(id);
    }

    default Klass getKlass(String id) {
        return getKlass(Id.parse(id));
    }

}
