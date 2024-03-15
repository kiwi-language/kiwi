package tech.metavm.object.type;

import tech.metavm.object.instance.core.Id;

public interface TypeProvider {

    Type getType(Id id);

    default Type getType(String id) {
        return getType(Id.parse(id));
    }

    default ClassType getClassType(Id id) {
        return (ClassType) getType(id);
    }

}
