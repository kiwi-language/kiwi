package tech.metavm.util;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.BuildKeyContext;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.GlobalKey;
import tech.metavm.object.instance.ColumnKind;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

@EntityType("列")
public record Column(
        @EntityField("列类型") ColumnKind kind,
        @EntityField(value = "列名", asTitle = true) String name,
        @EntityField(value = "标签") int tag
) implements GlobalKey {

    public static final Column ID = new Column(ColumnKind.INT, "id", 0);

    public static Column create(ColumnKind columnKind, int index) {
        return new Column(
                columnKind,
                String.format("%s%d", columnKind.prefix(), index),
                index << 3 | columnKind.tagSuffix()
        );
    }

    public static Column valueOf(String columnName) {
        if (columnName == null)
            return null;
        var sqlType = ColumnKind.getByPrefix(columnName.substring(0, 1));
        int index = Integer.parseInt(columnName.substring(1));
        return Column.create(sqlType, index);
    }

    public static Column allocate(Set<Column> usedColumns, ColumnKind columnKind) {
        Map<ColumnKind, Queue<Column>> columnMap = ColumnKind.getColumnMap(usedColumns);
        Queue<Column> columns = columnMap.get(columnKind);
        if (columns.isEmpty()) {
            throw BusinessException.tooManyFields();
        }
        return columns.poll();/*.copy();*/
    }

    public String fuzzyName() {
        if (kind != ColumnKind.STRING) {
            return name();
        }
        return "t" + name.substring(1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Column) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.kind, that.kind);
    }

    public boolean searchable() {
        return kind.esType() != null;
    }

    @Override
    public String toString() {
        return "Column[" +
                "name=" + name + ", " +
                "kind=" + kind + ']';
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return name;
    }
}
