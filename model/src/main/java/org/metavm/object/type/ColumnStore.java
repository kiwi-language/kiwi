package org.metavm.object.type;

import org.metavm.object.instance.ColumnKind;
import org.metavm.util.Column;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public interface ColumnStore {

    Column getColumn(Type type, Field field, ColumnKind columnKind);

    void save();
}
