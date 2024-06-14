package org.metavm.util;

import org.metavm.object.instance.core.BooleanInstance;
import org.metavm.object.instance.core.NullInstance;

public interface BuiltinInstanceHolder {

    NullInstance getNullInstance();

    void setNullInstance(NullInstance nullInstance);

    BooleanInstance getTrueInstance();

    void setTrueInstance(BooleanInstance trueInstance);

    BooleanInstance getFalseInstance();

    void setFalseInstance(BooleanInstance falseInstance);

}
