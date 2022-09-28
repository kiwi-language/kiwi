package tech.metavm.util;

import tech.metavm.object.instance.ColumnType;

public record Column(
        String name,
        ColumnType type
) {

    public static Column valueOf(String columnName) {
        if(columnName == null) {
            return null;
        }
        return new Column(columnName, ColumnType.getByColumnName(columnName));
    }

    public String fuzzyName() {
        if(type != ColumnType.VARCHAR64) {
            throw new UnsupportedOperationException("fuzzy name is only available for string columns");
        }
        return "t" + name.substring(1);
    }
}
