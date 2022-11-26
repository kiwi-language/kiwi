package tech.metavm.object.instance;

import tech.metavm.util.Column;
import tech.metavm.util.NncUtils;

import java.util.*;

public enum SQLColumnType {

    INT64("bigint", "long","l", 20),
    INT32("int", "integer", "i", 10),
    VARCHAR64("varchar(64)", "keyword","s", 10),
    BOOL("bool", "boolean",  "b",10),
    FLOAT("double", "double", "d", 10),
    TEXT("varchar(1024)", "text","t", 2),
    RELATION(null, "keyword", "r", 10);

    ;

    private final String sqlType;

    private final String esType;

    private final String prefix;

    private final int count;

    private static final List<Column> COLUMNS;

    private static final List<Column> SQL_COLUMNS;

    private static final List<String> SQL_COLUMNS_NAMES;

    public static final List<String> COLUMN_NAMES;

    final static Map<SQLColumnType, List<Column>> COLUMN_MAP;

    static {
        List<Column> columns = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        List<Column> sqlColumns = new ArrayList<>();
        Map<SQLColumnType, List<Column>> columnMap = new HashMap<>();
        for (SQLColumnType columnType : values()) {
            for(int i = 0; i < columnType.count; i++) {
                Column column = new Column(columnType.prefix()+ i, columnType);
                columns.add(column);
                columnNames.add(column.name());
                columnMap.computeIfAbsent(columnType, k -> new ArrayList<>()).add(column);
                if(columnType.sqlType != null) {
                    sqlColumns.add(column);
                }
            }
        }
        COLUMNS = Collections.unmodifiableList(columns);
        COLUMN_MAP = Collections.unmodifiableMap(columnMap);
        COLUMN_NAMES = Collections.unmodifiableList(columnNames);
        SQL_COLUMNS = sqlColumns;
        SQL_COLUMNS_NAMES = NncUtils.map(sqlColumns, Column::name);
    }

    SQLColumnType(String sqlName, String esType, String prefix, int count) {
        this.sqlType = sqlName;
        this.esType = esType;
        this.prefix = prefix;
        this.count = count;
    }

    public static SQLColumnType getByColumnName(String columnName) {
        for (SQLColumnType columnType : values()) {
            if(columnType.checkColumnName(columnName)) {
                return columnType;
            }
        }
        throw new RuntimeException("No column category found for column: " + columnName);
    }

    public String sqlName() {
        return sqlType;
    }

    public String prefix() {
        return prefix;
    }

    public int count() {
        return count;
    }

    public boolean checkColumnName(String columnName) {
        return columnName != null && prefix != null && columnName.startsWith(prefix);
    }

    public static Map<SQLColumnType, Queue<Column>> getColumnMap(List<Column> usedColumns) {
        Set<Column> usedColumnSet = new HashSet<>(usedColumns);
        Map<SQLColumnType, Queue<Column>> result = new HashMap<>();
        for (Column column : COLUMNS) {
            if(!usedColumnSet.contains(column)) {
                result.computeIfAbsent(column.type(), k -> new LinkedList<>()).add(column);
            }
        }
        return result;
    }

    public static List<String> sqlColumnNames() {
        return SQL_COLUMNS_NAMES;
    }

    public static List<Column> columns() {
        return COLUMNS;
    }

    public String esType() {
        return esType;
    }

}
