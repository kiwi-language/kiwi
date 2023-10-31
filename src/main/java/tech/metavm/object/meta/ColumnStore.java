package tech.metavm.object.meta;

import tech.metavm.object.instance.SQLType;
import tech.metavm.util.Column;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public interface ColumnStore {

    Column getColumn(Type type, Field field, SQLType sqlType);

    void save();
}
