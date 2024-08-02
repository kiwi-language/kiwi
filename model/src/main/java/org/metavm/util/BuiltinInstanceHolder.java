package org.metavm.util;

import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.NullValue;

public interface BuiltinInstanceHolder {

    NullValue getNullInstance();

    void setNullInstance(NullValue nullInstance);

    BooleanValue getTrueInstance();

    void setTrueInstance(BooleanValue trueInstance);

    BooleanValue getFalseInstance();

    void setFalseInstance(BooleanValue falseInstance);

}
