package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceFactory;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.rest.dto.*;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Null;

import java.util.Set;
import java.util.function.Function;

public class TypeFactory {

    private final Function<Class<?>, Type> getTypeFunc;

    public TypeFactory(Function<Class<?>, Type> getTypeFunc) {
        this.getTypeFunc = getTypeFunc;
    }

    public PrimitiveType createPrimitiveWithComposition(PrimitiveKind kind) {
        PrimitiveType primitiveType = createPrimitive(kind);
        TypeUtil.fillCompositeTypes(primitiveType, this);
        return primitiveType;
    }

    public PrimitiveType createPrimitive(PrimitiveKind kind) {
        return new PrimitiveType(kind);
    }

    public ClassType createAndBind(TypeDTO typeDTO, IEntityContext context) {
        ClassParamDTO param = (ClassParamDTO) typeDTO.param();
        ClassType type = new ClassType(
                typeDTO.name(),
                NncUtils.get(param.superTypeId(), context::getClassType),
                TypeCategory.getByCodeRequired(typeDTO.category()),
                typeDTO.anonymous(),
                typeDTO.ephemeral(),
                param.desc()
        );
        if(typeDTO.code() != null) {
            type.setCode(typeDTO.code());
        }
        context.bind(type);
        for (FieldDTO field : param.fields()) {
            createField(type, field, context);
        }
        for (ConstraintDTO constraint : param.constraints()) {
            ConstraintFactory.createFromDTO(constraint, context);
        }
        return type;
    }

    public boolean isNullable(Type type) {
        return type.isNullable();
    }

    private EnumConstantRT createEnumConstant(EnumType type, EnumConstantDTO ec) {
        return new EnumConstantRT(type, ec.name(), ec.ordinal());
    }

    public Field createField(ClassType declaringType, FieldDTO fieldDTO, IEntityContext context) {
        Type fieldType = context.getType(fieldDTO.typeId());
        Field field =  new Field(
                fieldDTO.name(),
                declaringType,
                Access.getByCodeRequired(fieldDTO.access()),
                fieldDTO.unique(),
                fieldDTO.asTitle(),
                InstanceFactory.resolveValue(fieldDTO.defaultValue(), fieldType, context),
                fieldType,
                fieldDTO.isChild()
        );
        context.bind(field);
        return field;
    }

    @SuppressWarnings("unused")
    public ClassType createClass(String name) {
        return createClass(name, null);
    }

    public ClassType createValueClass(String name) {
        return createValueClass(name, null);
    }

    public ClassType createValueClass(String name, ClassType superType) {
        return createValueClass(name, null, superType);
    }

    public ClassType createValueClass(String name, String code, ClassType superType) {
        ClassType type = createClass(name, superType, TypeCategory.VALUE);
        if(code != null) {
            type.setCode(code);
        }
        return type;
    }

    public ClassType createClass(String name, ClassType superType) {
        return createClass(name, null, superType);
    }

    public ClassType createClass(String name, String code, ClassType superType) {
        ClassType type =  createClass(name, superType, TypeCategory.CLASS);
        if(code != null) {
            type.setCode(code);
        }
        return type;
    }

    public ClassType createClass(String name, ClassType superType, TypeCategory category) {
        return new ClassType(
                name,
                superType,
                category,
                false,
                false,
                null
        );
    }

    public ArrayType createArrayType(Type elementType) {
        return new ArrayType(elementType, false);
    }

    public PrimitiveType getNullType() {
        return (PrimitiveType) getTypeFunc.apply(Null.class);
    }

    public EnumType createEnum(String name) {
        return createEnum(name, false);
    }

    public EnumType createEnum(String name, boolean anonymous) {
        return createEnum(name, anonymous, getEnumType());
    }

    public ClassType getEnumType() {
        return (ClassType) getTypeFunc.apply(Enum.class);
    }

    public AnyType getObjectType() {
        return (AnyType) getTypeFunc.apply(Object.class);
    }


    public ClassType getEntityType() {
        return (ClassType) getTypeFunc.apply(Entity.class);
    }

    public EnumType createEnum(String name, boolean anonymous, ClassType superType) {
        return createEnum(name, null, anonymous, superType);
    }

    public EnumType createEnum(String name, String code, boolean anonymous, ClassType superType) {
        EnumType enumType = new EnumType(name, superType, anonymous);
        if(code != null) {
            enumType.setCode(code);
        }
        return enumType;
    }

    public UnionType getNullableType(Type type) {
        UnionType nullableType = type.getNullableType();
        if(nullableType == null) {
            nullableType = createUnion(Set.of(type, getTypeFunc.apply(Null.class)));
            type.setNullableType(nullableType);
        }
        return nullableType;
    }

    public UnionType createUnion(Set<Type> types) {
        return new UnionType(types);
    }

}
