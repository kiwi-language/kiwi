package tech.metavm.object.instance;

import tech.metavm.constant.ColumnNames;
import tech.metavm.util.Column;

public class ObjectTableGenerator {

    public static final int NUM_INTEGER = 10;
    public static final int NUM_VARCHAR_64 = 10;
    public static final int NUM_BIGINT = 10;
    public static final int NUM_BOOL = 10;

    public static final int NUM_ARRAY_ELEMENTS = 256;

    public static void main(String[] args) {
        createReferenceArrayTable();
    }

    private static void createReferenceArrayTable() {
        TableSQLBuilder builder = new TableSQLBuilder("reference_array");
        addCommonColumns(builder);
        builder.addColumn("length", SQLColumnType.INT32, true, 0, null);
        for (int i = 0; i < NUM_ARRAY_ELEMENTS; i++) {
            builder.addColumn("r" + i, SQLColumnType.INT64);
        }
        System.out.println(builder.finish());
    }

    private static void addCommonColumns(TableSQLBuilder builder) {
        builder.addColumn(ColumnNames.ID, SQLColumnType.INT64, true, null, "ID", true, true);
        builder.addColumn(ColumnNames.TENANT_ID, SQLColumnType.INT64, true, null, "租户ID");
        builder.addColumn(ColumnNames.TYPE_ID, SQLColumnType.INT64, true, null, "类ID");
        builder.addColumn(ColumnNames.DELETED_AT, SQLColumnType.INT64, true, 0, "删除时间戳");

    }

    public static void createInstanceTable() {
        TableSQLBuilder builder = new TableSQLBuilder("instance");
        addCommonColumns(builder);

        for (Column column : SQLColumnType.columns()) {
            builder.addColumn(column.name(), column.type());
        }

        System.out.println(builder.finish());
    }


}
