package tech.metavm.object.type;

import tech.metavm.common.RefDTO;

public interface TypeProvider {

    Type getType(RefDTO ref);

    default Type getType(long id) {
        return getType(RefDTO.fromId(id));
    }

    default ClassType getClassType(long id) {
        return (ClassType) getType(id);
    }

    default ClassType getClassType(RefDTO ref) {
        return (ClassType) getType(ref);
    }

}
