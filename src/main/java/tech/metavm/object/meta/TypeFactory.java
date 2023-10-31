package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.GenericDeclaration;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceFactory;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.rest.dto.*;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Null;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class TypeFactory {

    public PrimitiveType createPrimitive(PrimitiveKind kind) {
        var type = new PrimitiveType(kind);
        if (isPutTypeSupported()) {
            putType(kind.getJavaClass(), type);
        }
        return type;
    }

    public ClassType createClassType(TypeDTO typeDTO, IEntityContext context) {
        ClassTypeParam param = typeDTO.getClassParam();
        var type = ClassBuilder.newBuilder(typeDTO.name(), typeDTO.code())
                .category(TypeCategory.getByCode(typeDTO.category()))
                .ephemeral(typeDTO.ephemeral())
                .anonymous(typeDTO.anonymous())
                .isTemplate(param.isTemplate())
                .typeParameters(NncUtils.map(param.typeParameterRefs(), context::getTypeVariable))
                .template(param.templateRef() != null ? context.getClassType(param.templateRef()) : null)
                .collectionName(param.templateName())
                .source(ClassSource.getByCode(param.source()))
                .tmpId(typeDTO.tmpId()).build();
        context.bind(type);
        return type;
    }

    public ClassType saveClassType(TypeDTO typeDTO, boolean withContent, IEntityContext context) {
        var param = typeDTO.getClassParam();
        var type = context.getClassType(typeDTO.getRef());
        if (type == null) {
            type = createClassType(typeDTO, context);
        }
        else {
            type.setCode(typeDTO.code());
            type.setName(typeDTO.name());
            if(typeDTO.category() == TypeCategory.ENUM.code()) {
                type.setSuperType(context.getParameterizedType(StandardTypes.getEnumType(), List.of(type)));
            }
            else {
                type.setSuperType(NncUtils.get(param.superTypeRef(), context::getClassType));
            }
            type.setInterfaces(NncUtils.map(param.interfaceRefs(), context::getClassType));
            type.setTypeArguments(NncUtils.map(param.typeArgumentRefs(), context::getType));
            type.setDesc(param.desc());
        }
        if (param.dependencyRefs() != null) {
            type.setDependencies(NncUtils.map(param.dependencyRefs(), context::getClassType));
        }
        var declaringType = type;
        if (withContent) {
            if (param.fields() != null) {
                type.setFields(NncUtils.map(param.fields(), f -> saveField(declaringType, f, context)));
            }
            if (param.staticFields() != null) {
                type.setStaticFields(NncUtils.map(param.staticFields(), f -> saveField(declaringType, f, context)));
            }
            if (param.constraints() != null) {
                type.setConstraints(NncUtils.map(param.constraints(), c -> ConstraintFactory.save(c, context)));
            }
        }
        return type;
    }

    public TypeVariable createTypeVariable(TypeDTO typeDTO,
                                           boolean withBounds, IEntityContext context) {
        var param = (TypeVariableParam) typeDTO.param();
        var typeVariable = new TypeVariable(typeDTO.tmpId(), typeDTO.name(), typeDTO.code(),
                context.getEntity(GenericDeclaration.class, param.genericDeclarationRef()));
        if (withBounds) {
            typeVariable.setBounds(NncUtils.map(param.boundRefs(), context::getType));
        }
        context.bind(typeVariable);
        return typeVariable;
    }

    public boolean isNullable(Type type) {
        return type.isNullable();
    }

//    public TypeVariable saveTypeVariable(TypeDTO typeDTO, ClassType declaringType, IEntityContext context) {
//        TypeVariable typeVariable = context.getTypeVariable(typeDTO.getRef());
//        if (typeVariable == null) {
//            typeVariable = new TypeVariable(typeDTO.tmpId(), typeDTO.name(), typeDTO.code(), declaringType);
//        } else {
//            typeVariable.setName(typeDTO.name());
//            typeVariable.setCode(typeDTO.code());
//        }
//        var param = (TypeVariableParam) typeDTO.param();
//        typeVariable.setBounds(NncUtils.map(param.boundRefs(), context::getType));
//        return typeVariable;
//    }

    public Field saveField(ClassType declaringType, FieldDTO fieldDTO, IEntityContext context) {
        Field field = context.getField(fieldDTO.getRef());
        Type fieldType = context.getType(fieldDTO.typeRef());
        var defaultValue = InstanceFactory.resolveValue(fieldDTO.defaultValue(), fieldType, context);
        var access = Access.getByCodeRequired(fieldDTO.access());
        if (field == null) {
            field = FieldBuilder.newBuilder(fieldDTO.name(), fieldDTO.code(), declaringType, fieldType)
                    .tmpId(fieldDTO.tmpId())
                    .access(access)
                    .unique(fieldDTO.unique())
                    .asTitle(fieldDTO.asTitle())
                    .defaultValue(defaultValue)
                    .isChild(fieldDTO.isChild())
                    .isStatic(fieldDTO.isStatic())
                    .staticValue(InstanceUtils.nullInstance())
                    .build();
            context.bind(field);
        } else {
            field.setName(fieldDTO.name());
            field.setCode(fieldDTO.code());
            field.setAsTitle(fieldDTO.asTitle());
            field.setUnique(fieldDTO.unique());
            field.setType(fieldType);
            field.setDefaultValue(defaultValue);
            field.setAccess(access);
        }
        return field;
    }

//    public UnionType getNullableType(Type type) {
//        UnionType nullableType = type.getNullableType();
//        if (nullableType == null) {
//            nullableType = createNullableType(type, null);
//            type.setNullableType(nullableType);
//        }
//        return nullableType;
//    }

//    public UnionType createNullableType(Type type, @Nullable TypeDTO typeDTO) {
//        var nullableType = createUnion(Set.of(type, getType(Null.class)));
//        type.setNullableType(nullableType);
//        if (typeDTO != null) {
//            nullableType.setTmpId(typeDTO.tmpId());
//        }
//        return nullableType;
//    }

    private Map<String, FlowDTO> getFlowDTOMap(@Nullable TypeDTO typeDTO) {
        Map<String, FlowDTO> flowDTOMap = new HashMap<>();
        if (typeDTO != null && typeDTO.param() instanceof ClassTypeParam param) {
            flowDTOMap.putAll(NncUtils.toMap(param.flows(), FlowDTO::code));
        }
        return flowDTOMap;
    }

    public void putType(java.lang.reflect.Type javaType, Type type) {
        throw new UnsupportedOperationException();
    }

    public void addType(Type type) {
        throw new UnsupportedOperationException();
    }

    public boolean isPutTypeSupported() {
        return false;
    }

    public boolean isAddTypeSupported() {
        return false;
    }

    public PrimitiveType getNullType() {
        return (PrimitiveType) getType(Null.class);
    }

    public PrimitiveType getStringType() {
        return (PrimitiveType) getType(String.class);
    }

    public abstract Type getType(java.lang.reflect.Type javaType);

    public ClassType getClassType(java.lang.reflect.Type javaType) {
        return (ClassType) getType(javaType);
    }

    public java.lang.reflect.Type getJavaType(Type type) {
        throw new UnsupportedOperationException();
    }

    public PrimitiveType getVoidType() {
        return (PrimitiveType) getType(Void.class);
    }

    public PrimitiveType getLongType() {
        return (PrimitiveType) getType(Long.class);
    }

    public ObjectType getObjectType() {
        return (ObjectType) getType(Object.class);
    }

    public ClassType getEntityType() {
        return (ClassType) getType(Entity.class);
    }

    public UnionType createUnion(Set<Type> types) {
        return new UnionType(null, types);
    }

    public PrimitiveType getBooleanType() {
        return (PrimitiveType) getType(Boolean.class);
    }

    public boolean containsJavaType(java.lang.reflect.Type javaType) {
        return getType(javaType) != null;
    }

}
