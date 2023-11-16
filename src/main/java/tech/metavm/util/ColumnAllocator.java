package tech.metavm.util;

import tech.metavm.object.instance.SQLType;
import tech.metavm.object.type.Type;

import java.util.*;

public class ColumnAllocator {

    private final Map<SQLType, Iterator<Column>> iterators = new HashMap<>();

    public ColumnAllocator() {
        this(List.of());
    }

    public ColumnAllocator(Collection<Column> usedColumns) {
        var columnMap = SQLType.getColumnMap(usedColumns);
        columnMap.forEach((sqlType, columns) -> iterators.put(sqlType, columns.iterator()));
    }

    public Column next(Type type) {
        return iterators.get(type.getSQLType()).next();
    }

}
