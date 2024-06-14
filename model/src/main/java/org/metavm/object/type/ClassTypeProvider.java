package org.metavm.object.type;

import org.metavm.common.RefDTO;

public interface ClassTypeProvider {

    Klass getClassType(RefDTO ref);

}
