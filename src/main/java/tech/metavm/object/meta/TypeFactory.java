package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.IEntityContext;
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
        context.bind(type);
        for (FieldDTO field : param.fields()) {
            createField(type, field, context);
        }
        for (ConstraintDTO constraint : param.constraints()) {
            ConstraintFactory.createFromDTO(constraint, type);
        }
        return type;
    }

    public boolean isNullable(Type type) {
        return type.isNullable();
    }

    private EnumConstantRT createEnumConstant(ClassType type, EnumConstantDTO ec) {
        return new EnumConstantRT(type, ec.name(), ec.ordinal());
    }

    public Field createField(ClassType type, FieldDTO fieldDTO, IEntityContext context) {
        Field field =  new Field(
                fieldDTO.name(),
                type,
                Access.getByCodeRequired(fieldDTO.access()),
                fieldDTO.unique(),
                fieldDTO.asTitle(),
                fieldDTO.defaultValue(),
                context.getType(fieldDTO.typeId()),
                fieldDTO.isChild()
        );
        context.bind(field);
        return field;
    }

    @SuppressWarnings("unused")
    public ClassType createClass(String name) {
        return createRefClass(name, null);
    }

    public ClassType createRefClass(String name, ClassType superType) {
        return createClass(name, superType, TypeCategory.CLASS);
    }

    public ClassType createValueClass(String name) {
        return createValueClass(name, null);
    }

    public ClassType createValueClass(String name, ClassType superType) {
        return createClass(name, superType, TypeCategory.VALUE);
    }

    public ClassType createClass(String name, ClassType superType) {
        return createClass(name, superType, TypeCategory.CLASS);
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

    public boolean isNullType(ClassType type) {
        return type == getNullType();
    }

    public Type getNullType() {
        return getTypeFunc.apply(Null.class);
    }

    public EnumType createEnum(String name) {
        return createEnum(name, false);
    }

    public EnumType createEnum(String name, boolean anonymous) {
        return createEnum(name, anonymous, getEnumType());
    }

    public EnumType getEnumType() {
        return (EnumType) getTypeFunc.apply(Enum.class);
    }

    public AnyType getObjectType() {
        return (AnyType) getTypeFunc.apply(Object.class);
    }


    public ClassType getEntityType() {
        return (ClassType) getTypeFunc.apply(Entity.class);
    }

    public EnumType createEnum(String name, boolean anonymous, ClassType superType) {
        return new EnumType(name, superType, anonymous);
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

//    public ParameterizedType createParameterized(ClassType rawType, List<Type> typeArguments) {
//        return new ParameterizedType(rawType, typeArguments);
//    }


}
