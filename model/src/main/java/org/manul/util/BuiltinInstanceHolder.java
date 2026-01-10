package org.manul.util;

import org.manul.object.instance.core.BooleanValue;
import org.manul.object.instance.core.NullValue;

public interface BuiltinInstanceHolder {

    NullValue getNullInstance();

    void setNullInstance(NullValue nullInstance);

    BooleanValue getTrueInstance();

    void setTrueInstance(BooleanValue trueInstance);

    BooleanValue getFalseInstance();

    void setFalseInstance(BooleanValue falseInstance);

}
