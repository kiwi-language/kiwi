package tech.metavm.object.meta;

import tech.metavm.object.instance.SQLType;
import tech.metavm.util.Column;
import tech.metavm.util.NncUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MemColumnStore implements ColumnStore {

    protected final Map<String, Map<String, String>> columnNameMap = new HashMap<>();

    public MemColumnStore() {
    }

    @Override
    public Column getColumn(Type type, Field field, SQLType sqlType) {
        String columnName = getSubMap(type).get(field.getName());
        return columnName != null ? SQLType.getColumnByName(columnName) :
                allocateColumn(type, field.getName(), sqlType);
    }

    private Column allocateColumn(Type type, String fieldName, SQLType sqlType) {
        var subMap = getSubMap(type);
        Set<Column> usedColumns = NncUtils.mapUnique(subMap.values(), SQLType::getColumnByName);
        var column = Column.allocate(usedColumns, sqlType);
        subMap.put(fieldName, column.name());
        return column;
    }

    private Map<String, String> getSubMap(Type type) {
        return columnNameMap.computeIfAbsent(type.getTypeName(), k -> new HashMap<>());
    }


    @Override
    public void save() {
    }
}
