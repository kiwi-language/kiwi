package tech.metavm.util;

import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.type.Type;

import java.util.*;

public class ColumnAllocator {

    private final Map<ColumnKind, Iterator<Column>> iterators = new HashMap<>();

    public ColumnAllocator() {
        this(List.of());
    }

    public ColumnAllocator(Collection<Column> usedColumns) {
        var columnMap = ColumnKind.getColumnMap(usedColumns);
        columnMap.forEach((sqlType, columns) -> iterators.put(sqlType, columns.iterator()));
    }

    public Column next(Type type) {
        return iterators.get(type.getSQLType()).next();
    }

}
