package tech.metavm.util;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.Model;
import tech.metavm.entity.ValueType;
import tech.metavm.object.instance.SQLColumnType;

import java.util.Objects;

@ValueType("列")
public record Column(
        @EntityField("列名") String name,
        @EntityField("列类型") SQLColumnType type
) {

    public static Column valueOf(String columnName) {
        if (columnName == null) {
            return null;
        }
        return new Column(columnName, SQLColumnType.getByColumnName(columnName));
    }

    public String fuzzyName() {
        if (type != SQLColumnType.VARCHAR64) {
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

    @Override
    public String toString() {
        return "Column[" +
                "name=" + name + ", " +
                "type=" + type + ']';
    }

}
