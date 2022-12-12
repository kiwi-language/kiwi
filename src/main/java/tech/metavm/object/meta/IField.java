package tech.metavm.object.meta;

import tech.metavm.object.instance.Instance;
import tech.metavm.util.Column;

public interface IField {

    String getName();

    Type getType();

    Type getDeclaringType();

    Column getColumn();

    default String getColumnName() {
        return getColumn().name();
    }

    String getDisplayValue(Instance value);

    Long getId();

    boolean isImplementation(IField that);

}
