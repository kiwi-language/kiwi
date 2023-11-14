package tech.metavm.util;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.GlobalKey;
import tech.metavm.entity.ValueType;
import tech.metavm.object.instance.SQLType;
import tech.metavm.object.meta.Type;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

@ValueType("列")
public record Column(
        @EntityField(value = "列名", asTitle = true) String name,
        @EntityField("列类型") SQLType type
) implements GlobalKey {

    public static final Column ID = new Column("id", SQLType.INT64);

    public static Column valueOf(String columnName) {
        if (columnName == null) {
            return null;
        }
        return new Column(columnName, SQLType.getByColumnName(columnName));
    }

    public static Column allocate(Set<Column> usedColumns, SQLType sqlType) {
        Map<SQLType, Queue<Column>> columnMap = SQLType.getColumnMap(usedColumns);
        Queue<Column> columns = columnMap.get(sqlType);
        if (columns.isEmpty()) {
            throw BusinessException.tooManyFields();
        }
        return columns.poll();/*.copy();*/
    }

    public String fuzzyName() {
        if (type != SQLType.VARCHAR64) {
            return name();
//            throw new UnsupportedOperationException("fuzzy name is only available for string columns");
        }
        return "t" + name.substring(1);
    }

    public Column copy() {
        return new Column(name, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Column) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.type, that.type);
    }

    public boolean searchable() {
        return type.esType() != null;
    }

    @Override
    public String toString() {
        return "Column[" +
                "name=" + name + ", " +
                "type=" + type + ']';
    }

    @Override
    public String getKey(Function<Type, java.lang.reflect.Type> getJavaType) {
        return name;
    }
}
