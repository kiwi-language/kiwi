package tech.metavm.object.type;

import tech.metavm.common.RefDTO;

public interface ClassTypeProvider {

    ClassType getClassType(RefDTO ref);

}
