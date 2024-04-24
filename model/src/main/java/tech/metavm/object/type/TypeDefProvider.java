package tech.metavm.object.type;

import tech.metavm.object.instance.core.Id;

public interface TypeDefProvider {

    TypeDef getTypeDef(Id id);

    default Klass getKlass(Id id) {
        return (Klass) getTypeDef(id);
    }

    default Klass getKlass(String id) {
        return getKlass(Id.parse(id));
    }

}
