package org.manul.object.type;

import org.manul.object.instance.core.Value;
import org.manul.util.Column;

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
