package tech.metavm.object.type;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.IndexDef;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.rest.dto.CreatingFieldDTO;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.util.Column;
import tech.metavm.util.Instances;

import javax.annotation.Nullable;

@EntityType
public class FieldData extends Entity {

    public static final IndexDef<FieldData> IDX_DECLARING_TYPE = IndexDef.create(FieldData.class, "declaringType");

    public static FieldData fromFieldDTO(FieldDTO fieldDTO, IEntityContext context) {
        var declaringType = context.getKlass(fieldDTO.declaringTypeId());
        var fieldType = TypeParser.parseType(fieldDTO.type(), context);
        var defaultValue = InstanceFactory.resolveValue(fieldDTO.defaultValue(), fieldType, context);
        var column = declaringType.allocateColumn(fieldType, null);
        return new FieldData(
                fieldDTO.tmpId(),
                fieldDTO.name(), fieldDTO.code(), column, fieldDTO.unique(),
                declaringType, Access.getByCode(fieldDTO.access()), fieldType, fieldDTO.isChild(),
                fieldDTO.isStatic(), Instances.nullInstance(), defaultValue);
    }

    private final String name;
    @Nullable
    private final String code;
    private final Column column;
    private final boolean unique;
    private final Klass declaringType;
    private final Access access;
    private final Type type;
    private final boolean isChild;
    private final boolean isStatic;
    private final Instance staticValue;
    @Nullable
    private final Instance defaultValue;

    public FieldData(Long tmpId, String name, @Nullable String code, Column column, boolean unique, Klass declaringType, Access access, Type type, boolean isChild, boolean isStatic, Instance staticValue, @Nullable Instance defaultValue) {
        super(tmpId);
        this.name = name;
        this.code = code;
        this.column = column;
        this.unique = unique;
        this.declaringType = declaringType;
        this.access = access;
        this.type = type;
        this.isChild = isChild;
        this.isStatic = isStatic;
        this.staticValue = staticValue;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public Column getColumn() {
        return column;
    }

    public boolean isUnique() {
        return unique;
    }

    public Klass getDeclaringType() {
        return declaringType;
    }

    public Access getAccess() {
        return access;
    }

    public Type getType() {
        return type;
    }

    public boolean isChild() {
        return isChild;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public Instance getStaticValue() {
        return staticValue;
    }

    @Nullable
    public Instance getDefaultValue() {
        return defaultValue;
    }

    public CreatingFieldDTO toDTO() {
        return new CreatingFieldDTO(
                name,
                code,
                type.getStringId(),
                type.getName(),
                unique
        );
    }

}
