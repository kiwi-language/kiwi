package org.metavm.object.type;

import org.metavm.object.instance.ColumnKind;
import org.metavm.util.ColumnAndTag;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public interface ColumnStore {

    ColumnAndTag getColumn(Type type, Field field, ColumnKind columnKind);

    void save();
}
