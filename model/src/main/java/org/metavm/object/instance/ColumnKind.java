package org.metavm.object.instance;

import org.metavm.util.Column;
import org.metavm.util.NncUtils;

import java.util.*;

public enum ColumnKind {

    INT(1, "bigint", "long", "l", 20) ,

    STRING(2, "varchar(256)","keyword","s",10),

    BOOL(3, "bool","boolean","b",20),

    DOUBLE(4, "double","double","d",10),

    REFERENCE(5, "bigint","long","r",30),

    UNSPECIFIED(6, "json",null,"o",10),

    ;

    private final int tagSuffix;

    private final String sqlType;

    private final String esType;

    private final String prefix;

    private final int count;

    private static final List<Column> COLUMNS;

    private static final List<String> SQL_COLUMNS_NAMES;

    public static final List<String> COLUMN_NAMES;

    public static final Map<String, Column> NAME_2_COLUMN;

    public static final Map<Integer, Column> TAG_2_COLUMN;

    final static Map<ColumnKind, List<Column>> COLUMN_MAP;

    static {
        List<Column> columns = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        List<Column> sqlColumns = new ArrayList<>();
        Map<ColumnKind, List<Column>> columnMap = new HashMap<>();
        Map<String, Column> name2col = new HashMap<>();
        for (ColumnKind columnType : values()) {
            for (int i = 0; i < columnType.count; i++) {
                Column column = Column.create(columnType, i);
                columns.add(column);
                columnNames.add(column.name());
                columnMap.computeIfAbsent(columnType, k -> new ArrayList<>()).add(column);
                name2col.put(column.name(), column);
                if (columnType.sqlType != null) {
                    sqlColumns.add(column);
                }
            }
        }
        NAME_2_COLUMN = Collections.unmodifiableMap(name2col);
        COLUMNS = Collections.unmodifiableList(columns);
        COLUMN_MAP = Collections.unmodifiableMap(columnMap);
        COLUMN_NAMES = Collections.unmodifiableList(columnNames);
        SQL_COLUMNS_NAMES = NncUtils.map(sqlColumns, Column::name);
        TAG_2_COLUMN = NncUtils.toMap(sqlColumns, Column::tag);
    }

    ColumnKind(int tagSuffix, String sqlName, String esType, String prefix, int count) {
        this.tagSuffix = tagSuffix;
        this.sqlType = sqlName;
        this.esType = esType;
        this.prefix = prefix;
        this.count = count;
    }

    public static Column getByTag(int tag) {
        return TAG_2_COLUMN.get(tag);
    }

    public static ColumnKind getByPrefix(String prefix) {
        return NncUtils.findRequired(values(), v -> v.prefix.equals(prefix));
    }

    public Column getColumn(int index) {
        return COLUMN_MAP.get(this).get(index);
    }

    public static Column getColumnByName(String name) {
        return NncUtils.requireNonNull(NAME_2_COLUMN.get(name),
                "Can not find column with name '" + name + "'");
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

    public int tagSuffix() {
        return tagSuffix;
    }

    public boolean checkColumnName(String columnName) {
        return columnName != null && prefix != null && columnName.startsWith(prefix);
    }

    public static Map<ColumnKind, Queue<Column>> getColumnMap(Collection<Column> usedColumns) {
        Set<Column> usedColumnSet = new HashSet<>(usedColumns);
        Map<ColumnKind, Queue<Column>> result = new HashMap<>();
        for (Column column : COLUMNS) {
            if (!usedColumnSet.contains(column)) {
                result.computeIfAbsent(column.kind(), k -> new LinkedList<>()).add(column);
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
