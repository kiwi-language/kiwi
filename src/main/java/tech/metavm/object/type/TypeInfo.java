//package tech.metavm.object.meta;
//
//import tech.metavm.object.instance.SQLColumnType;
//import tech.metavm.object.meta.persistence.TypePO;
//
//import java.util.List;
//import java.util.Objects;
//import java.util.Set;
//
//public final class TypeInfo {
//    private final long id;
//    private final String name;
//    private final SQLColumnType sqlColumnType;
//    private final TypeCategory category;
//    private final List<FieldInfo> fields;
//    private final List<UniqueConstraintInfo> uniqueConstraints;
//
//    public TypeInfo(
//            long id,
//            String name,
//            SQLColumnType sqlColumnType,
//            TypeCategory category,
//            List<FieldInfo> fields,
//            List<UniqueConstraintInfo> uniqueConstraints
//    ) {
//        this.id = id;
//        this.name = name;
//        this.sqlColumnType = sqlColumnType;
//        this.category = category;
//        this.fields = fields;
//        this.uniqueConstraints = uniqueConstraints;
//    }
//
//    static TypeInfo createPrimitive(long id, String name, SQLColumnType columnType) {
//        return new TypeInfo(
//                id, name, columnType, TypeCategory.PRIMITIVE, List.of(), List.of()
//        );
//    }
//
//    static TypeInfo create(long id, String name, TypeCategory category, SQLColumnType sqlColumnType) {
//        return create(id, name, category, sqlColumnType, List.of(), List.of());
//    }
//
//    static TypeInfo createClass(long id, String name, List<FieldInfo> fields, List<UniqueConstraintInfo> uniqueConstraints) {
//        return new TypeInfo(id, name, SQLColumnType.INT64, TypeCategory.CLASS, fields, uniqueConstraints);
//    }
//
//    static TypeInfo createClass(long id, String name, List<FieldInfo> fields) {
//        return createClass(id, name, fields, List.of());
//    }
//
//    static TypeInfo createClass(long id, String name) {
//        return createClass(id, name, List.of(), List.of());
//    }
//
//    static TypeInfo create(
//            long id, String name,
//            TypeCategory category,
//            SQLColumnType sqlColumnType,
//            List<FieldInfo> fields) {
//        return new TypeInfo(id, name, sqlColumnType, category, fields, List.of());
//    }
//
//    static TypeInfo create(
//            long id, String name,
//            TypeCategory category,
//            SQLColumnType sqlColumnType,
//            List<FieldInfo> fields,
//            List<UniqueConstraintInfo> uniqueConstraints
//    ) {
//        return new TypeInfo(id, name, sqlColumnType, category, fields, uniqueConstraints);
//    }
//
//    public TypePO toPO() {
//        TypePO typePO = new TypePO();
//        typePO.setTenantId(-1L);
//        typePO.setId(id);
//        typePO.setCategory(category.code());
//        typePO.setName(name);
//        typePO.setEphemeral(false);
//        typePO.setAnonymous(false);
//        typePO.setDesc(name);
//        typePO.setTypeArgumentIds(List.of());
//        return typePO;
//    }
//
//    public long getArrayId() {
//        return StdTypeManager.getArrayId(id);
//    }
//
//    public TypePO getArrayPO() {
//        TypePO po = new TypePO();
//        po.setTenantId(-1L);
//        po.setId(getArrayId());
//        po.setCategory(TypeCategory.ARRAY.code());
//        po.setRawTypeId(IdConstants.ARRAY);
//        po.setTypeArgumentIds(List.of(id));
//        po.setName(name + "[]");
//        po.setEphemeral(false);
//        po.setAnonymous(false);
//        po.setDesc(null);
//        return po;
//    }
//
//    public long getNullableId() {
//        return StdTypeManager.getNullableId(id);
//    }
//
//    public TypePO getNullablePO() {
//        TypePO po = new TypePO();
//        po.setTenantId(-1L);
//        po.setId(getNullableId());
//        po.setCategory(TypeCategory.UNION.code());
////        po.setRawTypeId(IdConstants.NULLABLE);
////        po.setTypeArgumentIds(List.of(id));
//        po.setTypeMemberIds(Set.of(id, IdConstants.NULL));
//        po.setName(name + "?");
//        po.setEphemeral(false);
//        po.setAnonymous(false);
//        po.setDesc(null);
//        return po;
//    }
//
//    public long id() {
//        return id;
//    }
//
//    public String name() {
//        return name;
//    }
//
//    public SQLColumnType sqlColumnType() {
//        return sqlColumnType;
//    }
//
//    public TypeCategory category() {
//        return category;
//    }
//
//    public List<FieldInfo> fields() {
//        return fields;
//    }
//
//    public List<UniqueConstraintInfo> uniqueConstraints() {
//        return uniqueConstraints;
//    }
//
//    public void addField(FieldInfo indexItem) {
//        fields.add(indexItem);
//    }
//
//    public void addUniqueConstraint(UniqueConstraintInfo uniqueConstraint) {
//        uniqueConstraints.add(uniqueConstraint);
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == this) return true;
//        if (obj == null || obj.getClass() != this.getClass()) return false;
//        var that = (TypeInfo) obj;
//        return this.id == that.id &&
//                Objects.equals(this.name, that.name) &&
//                Objects.equals(this.sqlColumnType, that.sqlColumnType) &&
//                Objects.equals(this.category, that.category) &&
//                Objects.equals(this.fields, that.fields) &&
//                Objects.equals(this.uniqueConstraints, that.uniqueConstraints);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(id, name, sqlColumnType, category, fields, uniqueConstraints);
//    }
//
//    @Override
//    public String toString() {
//        return "TypeInfo[" +
//                "id=" + id + ", " +
//                "name=" + name + ", " +
//                "sqlColumnType=" + sqlColumnType + ", " +
//                "category=" + category + ", " +
//                "fields=" + fields + ", " +
//                "uniqueConstraints=" + uniqueConstraints + ']';
//    }
//
//
//}
