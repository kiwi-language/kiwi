package tech.metavm.object.type;

import tech.metavm.common.RefDTO;
import tech.metavm.flow.Method;

public interface MethodProvider {

    Method getMethod(RefDTO ref);

}
