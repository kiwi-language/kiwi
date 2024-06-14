package org.metavm.object.type;

import org.metavm.common.RefDTO;
import org.metavm.flow.Method;

public interface MethodProvider {

    Method getMethod(RefDTO ref);

}
