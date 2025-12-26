package org.metavm.util;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.Generated;
import org.metavm.api.ValueObject;
import org.metavm.entity.BuildKeyContext;
import org.metavm.wire.Wire;
import org.metavm.entity.GlobalKey;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.Reference;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

@Wire
@Entity
public record Column(
        ColumnKind kind,
        @EntityField(asTitle = true) String name,
        int tag
) implements GlobalKey, ValueObject {

    public static final Column ID = new Column(ColumnKind.INT, "id", 0);
    public static final Column NIL = new Column(ColumnKind.UNSPECIFIED, "nil", -1);

    public static Column create(ColumnKind columnKind, int index) {
        return new Column(
                columnKind,
                String.format("%s%d", columnKind.prefix(), index),
                index << 3 | columnKind.code()
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

    @Generated
    public static Column read(MvInput input) {
        return new Column(ColumnKind.fromCode(input.read()), input.readUTF(), input.readInt());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitByte();
        visitor.visitUTF();
        visitor.visitInt();
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

    public void forEachReference(Consumer<Reference> action) {
    }

    public void buildJson(Map<String, Object> map) {
        map.put("kind", this.kind().name());
        map.put("name", this.name());
        map.put("tag", this.tag());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(kind.code());
        output.writeUTF(name);
        output.writeInt(tag);
    }

    public Map<String, Object> toJson() {
        var map = new java.util.HashMap<String, Object>();
        buildJson(map);
        return map;
    }
}
