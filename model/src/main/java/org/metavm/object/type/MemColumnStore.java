package org.metavm.object.type;

import org.metavm.object.instance.ColumnKind;
import org.metavm.util.Column;
import org.metavm.util.ColumnAndTag;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MemColumnStore implements ColumnStore {

    private static final Logger logger = LoggerFactory.getLogger(MemColumnStore.class);

    protected final Map<String, Map<String, String>> columnNameMap = new HashMap<>();
    protected final Map<String, Map<String, Integer>> tagMap = new HashMap<>();

    public MemColumnStore() {
    }

    @Override
    public ColumnAndTag getColumn(Type type, Field field, ColumnKind columnKind) {
        String columnName = getSubMap(type).get(field.getName());
        var col = columnName != null ? ColumnKind.getColumnByName(columnName) :
                allocateColumn(type, field.getName(), columnKind);
        var tag = getTagSubMap(type).get(field.getName());
        if(tag == null)
            tag = allocateTag(type, field.getName());
        return new ColumnAndTag(col, tag);
    }

    private Column allocateColumn(Type type, String fieldName, ColumnKind columnKind) {
        var subMap = getSubMap(type);
        Set<Column> usedColumns = Utils.mapToSet(subMap.values(), ColumnKind::getColumnByName);
        var column = Column.allocate(usedColumns, columnKind);
        subMap.put(fieldName, column.name());
        return column;
    }

    private int allocateTag(Type type, String fieldName) {
        var subMap = getTagSubMap(type);
        int max = -1;
        for (Integer value : subMap.values()) {
            max = Math.max(value, max);
        }
        var tag = max + 1;
        subMap.put(fieldName, tag);
        return tag;
    }

    private Map<String, Integer> getTagSubMap(Type type) {
        return tagMap.computeIfAbsent(type.getTypeName(), k -> new HashMap<>());
    }

    private Map<String, String> getSubMap(Type type) {
        return columnNameMap.computeIfAbsent(type.getTypeName(), k -> new HashMap<>());
    }


    @Override
    public void save() {
    }

    public MemColumnStore copy() {
        var copy = new MemColumnStore();
        columnNameMap.forEach((className, names) -> {
            var copyNames = new HashMap<String, String>();
            copy.columnNameMap.put(className, copyNames);
            copyNames.putAll(names);
        });
        tagMap.forEach((className, tags) -> {
            var copyTags = new HashMap<String, Integer>();
            copy.tagMap.put(className, copyTags);
            copyTags.putAll(tags);
        });
        return copy;
    }
}
