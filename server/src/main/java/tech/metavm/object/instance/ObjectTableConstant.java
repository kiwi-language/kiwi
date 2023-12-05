package tech.metavm.object.instance;

import tech.metavm.constant.ColumnNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectTableConstant {

    public static final String TABLE_INSTANCE = "instance";

    public static final int NUM_INTEGER = 10;
    public static final int NUM_VARCHAR_64 = 10;
    public static final int NUM_BIGINT = 10;
    public static final int NUM_BOOL = 10;

    public static final List<String> INSERT_COLUMNS;

    static {

        List<String> columns = new ArrayList<>();

        columns.add(ColumnNames.APPLICATION_ID);
        columns.add(ColumnNames.TYPE_ID);

        for(int i = 0; i < NUM_BIGINT; i++) {
            columns.add("l" + i);
        }

        for(int i = 0; i < NUM_BOOL; i++) {
            columns.add("b" + i);
        }

        for(int i = 0; i < NUM_INTEGER; i++) {
            columns.add("i" + i);
        }

        for(int i = 0; i < NUM_VARCHAR_64; i++) {
            columns.add("s" + i);
        }

        INSERT_COLUMNS = Collections.unmodifiableList(columns);
    }
}
