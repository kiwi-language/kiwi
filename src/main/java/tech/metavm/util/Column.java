package tech.metavm.util;

import tech.metavm.object.instance.SQLColumnType;

public record Column(
        String name,
        SQLColumnType type
) {

    public static Column valueOf(String columnName) {
        if(columnName == null) {
            return null;
        }
        return new Column(columnName, SQLColumnType.getByColumnName(columnName));
    }

    public String fuzzyName() {
        if(type != SQLColumnType.VARCHAR64) {
            throw new UnsupportedOperationException("fuzzy name is only available for string columns");
        }
        return "t" + name.substring(1);
    }
}
