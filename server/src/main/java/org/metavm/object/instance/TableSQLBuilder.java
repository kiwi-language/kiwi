package org.metavm.object.instance;

import org.metavm.object.instance.ColumnKind;

public class TableSQLBuilder {

    private final StringBuffer buf = new StringBuffer();
    private int numColumns = 0;

    public TableSQLBuilder(String tableName) {
        buf.append("CREATE TABLE `")
                .append(tableName)
                .append("` (");
    }

    public void addColumn(String name, ColumnKind type) {
        addColumn(name, type, false, null, null);
    }

    public void addColumn(String name, ColumnKind type, boolean notNull, Object defaultValue, String comment) {
        addColumn(name, type, notNull, defaultValue, comment, false, false);
    }

    public void addColumn(String name, ColumnKind type, boolean notNull, Object defaultValue,
                          String comment, boolean primaryKey, boolean autoIncrement) {
        if(numColumns > 0) {
            buf.append(", ");
        }
        numColumns++;
        buf.append("`").append(name).append("` ").append(type.sqlName());
        if(notNull) {
            buf.append(" NOT NULL");
        }
        if(!notNull || defaultValue != null) {
            buf.append(" DEFAULT ").append(convertDefaultValue(defaultValue));
        }
        if(primaryKey) {
            buf.append(" PRIMARY KEY");
        }
        if(autoIncrement) {
            buf.append(" AUTO_INCREMENT");
        }
        if(comment != null) {
            buf.append(" COMMENT '").append(comment).append("'");
        }
    }

    public String finish() {
        buf.append(")");
        return buf.toString();
    }

    private String convertDefaultValue(Object value) {
        if(value == null) {
            return "NULL";
        }
        if(value instanceof String) {
            return "'" + value + "'";
        }
        return value.toString();
    }

}
