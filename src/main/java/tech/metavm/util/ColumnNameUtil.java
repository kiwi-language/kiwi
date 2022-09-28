package tech.metavm.util;

import tech.metavm.object.instance.ColumnType;

import java.util.ArrayList;
import java.util.List;

public class ColumnNameUtil {

    public static final int NUM_INTEGER = 10;
    public static final int NUM_VARCHAR_64 = 10;
    public static final int NUM_BIGINT = 10;
    public static final int NUM_BOOL = 10;
    public static final int NUM_FLOAT = 10;

    public List<Column> getColumnNames() {
        List<Column> columns = new ArrayList<>();
        addColumns(columns, NUM_INTEGER, ColumnType.INT32);
        addColumns(columns, NUM_BIGINT, ColumnType.INT32);
        return columns;
    }

    private void addColumns(List<Column> columns, int count, ColumnType columnType) {
        for(int i = 0; i < count; i++) {
            columns.add(new Column(columnType.prefix()+ i, columnType));
        }
    }


}
