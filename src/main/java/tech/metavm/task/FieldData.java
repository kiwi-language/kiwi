package tech.metavm.task;

import tech.metavm.entity.*;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Access;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.rest.dto.CreatingFieldDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.Column;
import tech.metavm.util.InstanceUtils;

import javax.annotation.Nullable;

@EntityType("字段信息")
public class FieldData extends Entity {

    public static final IndexDef<FieldData> IDX_DECLARING_TYPE = IndexDef.normalKey(FieldData.class, "declaringType");

    public static FieldData fromFieldDTO(FieldDTO fieldDTO, IEntityContext context) {
        var declaringType = context.getClassType(fieldDTO.declaringTypeId());
        var fieldType = context.getType(fieldDTO.typeRef());
        var defaultValue = InstanceFactory.resolveValue(fieldDTO.defaultValue(), fieldType, context);
        var column = declaringType.allocateColumn(fieldType, null);
        FieldData data = new FieldData(
                fieldDTO.tmpId(),
                fieldDTO.name(), fieldDTO.code(), column, fieldDTO.unique(), fieldDTO.asTitle(),
                declaringType, Access.getByCodeRequired(fieldDTO.access()), fieldType, fieldDTO.isChild(),
                fieldDTO.isStatic(), InstanceUtils.nullInstance(), defaultValue);
        return data;
    }

    private final String name;
    @Nullable
    private final String code;
    private final Column column;
    private final boolean unique;
    private final boolean asTitle;
    private final ClassType declaringType;
    private final Access access;
    private final Type type;
    private final boolean isChild;
    private final boolean isStatic;
    private final Instance staticValue;
    private final Instance defaultValue;

    public FieldData(Long tmpId, String name, String code, Column column, boolean unique, boolean asTitle, ClassType declaringType, Access access, Type type, boolean isChild, boolean isStatic, Instance staticValue, Instance defaultValue) {
        super(tmpId);
        this.name = name;
        this.code = code;
        this.column = column;
        this.unique = unique;
        this.asTitle = asTitle;
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

    public boolean isAsTitle() {
        return asTitle;
    }

    public ClassType getDeclaringType() {
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

    public Instance getDefaultValue() {
        return defaultValue;
    }

    public CreatingFieldDTO toDTO() {
        return new CreatingFieldDTO(
                name,
                code,
                type.getIdRequired(),
                type.getName(),
                unique
        );
    }

}
