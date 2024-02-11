package tech.metavm.util;

import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.NullInstance;

public interface BuiltinInstanceHolder {

    NullInstance getNullInstance();

    void setNullInstance(NullInstance nullInstance);

    BooleanInstance getTrueInstance();

    void setTrueInstance(BooleanInstance trueInstance);

    BooleanInstance getFalseInstance();

    void setFalseInstance(BooleanInstance falseInstance);

}
