package tech.metavm.object.instance;

import tech.metavm.util.Column;

public class ObjectTableGenerator {

    public static final int NUM_INTEGER = 10;
    public static final int NUM_VARCHAR_64 = 10;
    public static final int NUM_BIGINT = 10;
    public static final int NUM_BOOL = 10;

    public static void main(String[] args) {
        TableSQLBuilder builder = new TableSQLBuilder("instance");

        builder.addColumn("id", ColumnType.INT64, true, null, "ID", true, true);
        builder.addColumn("tenant_id", ColumnType.INT64, true, null, "租户ID");
        builder.addColumn("n_class_id", ColumnType.INT64, true, null, "类ID");
        builder.addColumn("deleted_at", ColumnType.INT64, true, 0, "删除时间戳");
//
//        for(int i = 0; i < NUM_INTEGER; i++) {
//            builder.addColumn("i" + i, ColumnType.INT);
//        }
//        for(int i = 0; i < NUM_BIGINT; i++) {
//            builder.addColumn("l" + i, ColumnType.BIGINT);
//        }
//        for(int i = 0; i < NUM_VARCHAR_64; i++) {
//            builder.addColumn("s" + i, ColumnType.VARCHAR64);
//        }
//        for(int i = 0; i < NUM_BOOL; i++) {
//            builder.addColumn("b" + i, ColumnType.BOOL);
//        }

        for (Column column : ColumnType.columns()) {
            builder.addColumn(column.name(), column.type());
        }

        System.out.println(builder.finish());
    }


}
