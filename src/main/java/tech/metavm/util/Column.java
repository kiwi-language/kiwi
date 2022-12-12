package tech.metavm.util;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.ValueType;
import tech.metavm.object.instance.SQLType;

import java.util.Objects;

@ValueType("列")
public record Column(
        @EntityField("列名") String name,
        @EntityField("列类型") SQLType type
) {

    public static Column valueOf(String columnName) {
        if (columnName == null) {
            return null;
        }
        return new Column(columnName, SQLType.getByColumnName(columnName));
    }

    public String fuzzyName() {
        if (type != SQLType.VARCHAR64) {
            throw new UnsupportedOperationException("fuzzy name is only available for string columns");
        }
        return "t" + name.substring(1);
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

}
