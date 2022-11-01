package tech.metavm.object.meta;

import tech.metavm.object.instance.SQLColumnType;
import tech.metavm.object.meta.persistence.TypePO;

import java.util.List;

public record TypeInfo(
        long id,
        String name,
        SQLColumnType sqlColumnType,
        TypeCategory category,
        List<FieldInfo> fields,
        List<UniqueConstraintInfo> uniqueConstraints
) {

    static TypeInfo createPrimitive(long id, String name, SQLColumnType columnType) {
        return new TypeInfo(
                id, name, columnType, TypeCategory.PRIMITIVE, List.of(), List.of()
        );
    }

    static TypeInfo create(long id, String name, TypeCategory category, SQLColumnType sqlColumnType) {
        return create(id, name, category, sqlColumnType, List.of(), List.of());
    }

    static TypeInfo create(
            long id, String name,
            TypeCategory category,
            SQLColumnType sqlColumnType,
            List<FieldInfo> fields) {
        return new TypeInfo(id, name, sqlColumnType, category, fields, List.of());
    }

    static TypeInfo create(
            long id, String name,
            TypeCategory category,
            SQLColumnType sqlColumnType,
            List<FieldInfo> fields,
            List<UniqueConstraintInfo> uniqueConstraints
    ) {
        return new TypeInfo(id, name, sqlColumnType, category, fields, uniqueConstraints);
    }

    public TypePO toPO() {
        TypePO typePO = new TypePO();
        typePO.setTenantId(-1L);
        typePO.setId(id);
        typePO.setCategory(category.code());
        typePO.setName(name);
        typePO.setEphemeral(false);
        typePO.setAnonymous(false);
        typePO.setDesc(name);
        typePO.setTypeArgumentIds(List.of());
        return typePO;
    }

    public long getArrayId() {
        return StdTypeManager.getArrayId(id);
    }

    public TypePO getArrayPO() {
        TypePO po = new TypePO();
        po.setTenantId(-1L);
        po.setId(getArrayId());
        po.setCategory(TypeCategory.ARRAY.code());
        po.setRawTypeId(StdTypeConstants.ARRAY);
        po.setTypeArgumentIds(List.of(id));
        po.setName(name + "[]");
        po.setEphemeral(false);
        po.setAnonymous(false);
        po.setDesc(null);
        return po;
    }

    public long getNullableId() {
        return StdTypeManager.getNullableId(id);
    }

    public TypePO getNullablePO() {
        TypePO po = new TypePO();
        po.setTenantId(-1L);
        po.setId(getNullableId());
        po.setCategory(TypeCategory.NULLABLE.code());
        po.setRawTypeId(StdTypeConstants.NULLABLE);
        po.setTypeArgumentIds(List.of(id));
        po.setName(name + "?");
        po.setEphemeral(false);
        po.setAnonymous(false);
        po.setDesc(null);
        return po;
    }

}
