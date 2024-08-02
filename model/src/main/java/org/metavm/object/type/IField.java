package org.metavm.object.type;

import org.metavm.object.instance.core.Value;
import org.metavm.util.Column;

public interface IField {

    String getName();

    Type getType();

    Type getDeclaringType();

    Column getColumn();

    default String getColumnName() {
        return getColumn().name();
    }

    String getDisplayValue(Value value);

    Long getId();

    boolean isImplementation(IField that);

}
