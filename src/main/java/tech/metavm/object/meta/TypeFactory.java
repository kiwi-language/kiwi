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
import java.util.*;

public abstract class TypeFactory {

    public PrimitiveType createPrimitiveWithComposition(PrimitiveKind kind) {
        PrimitiveType primitiveType = createPrimitive(kind);
        TypeUtil.fillCompositeTypes(primitiveType, this);
        return primitiveType;
    }

    public PrimitiveType createPrimitive(PrimitiveKind kind) {
        var type = new PrimitiveType(kind);
        if (isPutTypeSupported()) {
            putType(kind.getJavaClass(), type);
        }
        return type;
    }

    public ClassType createClassType(TypeDTO typeDTO, IEntityContext context) {
        ClassParamDTO param = typeDTO.getClassParam();
        var type = ClassBuilder.newBuilder(typeDTO.name(), typeDTO.code())
                .category(TypeCategory.getByCode(typeDTO.category()))
                .ephemeral(typeDTO.ephemeral())
                .anonymous(typeDTO.anonymous())
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
        if(type == null) {
            type = createClassType(typeDTO, context);
        }
        type.setSuperType(NncUtils.get(param.superTypeRef(), context::getClassType));
        type.setInterfaces(NncUtils.map(param.interfaceRefs(), context::getClassType));
        type.setTypeArguments(NncUtils.map(param.typeArgumentRefs(), context::getType));
        type.setDesc(param.desc());
        if(param.dependencyRefs() != null) {
            type.setDependencies(NncUtils.map(param.dependencyRefs(), context::getClassType));
        }
        if (withContent) {
            for (FieldDTO field : param.fields()) {
                createField(type, field, context);
            }
            for (FieldDTO field : param.staticFields()) {
                createField(type, field, context);
            }
            for (ConstraintDTO constraint : param.constraints()) {
                ConstraintFactory.createFromDTO(constraint, context);
            }
        }
        return type;
    }

    public TypeVariable createTypeVariable(TypeDTO typeDTO,
                                           boolean withBounds, IEntityContext context) {
        var param = (TypeVariableParamDTO) typeDTO.param();
        var typeVariable = new TypeVariable(typeDTO.tmpId(), typeDTO.name(), typeDTO.code());
        typeVariable.setGenericDeclaration(
                context.getEntity(GenericDeclaration.class, param.genericDeclarationRef())
        );
        if (withBounds) {
            typeVariable.setBounds(NncUtils.map(param.boundRefs(), context::getType));
        }
        context.bind(typeVariable);
        return typeVariable;
    }

    public boolean isNullable(Type type) {
        return type.isNullable();
    }

    public Field createField(ClassType declaringType, FieldDTO fieldDTO, IEntityContext context) {
        Type fieldType = context.getType(fieldDTO.typeRef());
        Field field = FieldBuilder.newBuilder(fieldDTO.name(), fieldDTO.code(), declaringType, fieldType)
                .tmpId(fieldDTO.tmpId())
                .access(Access.getByCodeRequired(fieldDTO.access()))
                .unique(fieldDTO.unique())
                .asTitle(fieldDTO.asTitle())
                .defaultValue(InstanceFactory.resolveValue(fieldDTO.defaultValue(), fieldType, context))
                .isChild(fieldDTO.isChild())
                .isStatic(fieldDTO.isStatic())
                .staticValue(InstanceUtils.nullInstance())
                .build();
        context.bind(field);
        return field;
    }

    public UnionType getNullableType(Type type) {
        UnionType nullableType = type.getNullableType();
        if (nullableType == null) {
            nullableType = createNullableType(type, null);
            type.setNullableType(nullableType);
        }
        return nullableType;
    }

    public UnionType createNullableType(Type type, @Nullable TypeDTO typeDTO) {
        var nullableType = createUnion(Set.of(type, getType(Null.class)));
        type.setNullableType(nullableType);
        if (typeDTO != null) {
            nullableType.setTmpId(typeDTO.tmpId());
        }
        return nullableType;
    }

    public ArrayType createArrayType(Type elementType) {
        return createArrayType(elementType, null);
    }

    public ArrayType createArrayType(Type elementType, @Nullable TypeDTO typeDTO) {
        return new ArrayType(NncUtils.get(typeDTO, TypeDTO::tmpId), elementType, false);
    }

    private Map<String, FlowDTO> getFlowDTOMap(@Nullable TypeDTO typeDTO) {
        Map<String, FlowDTO> flowDTOMap = new HashMap<>();
        if (typeDTO != null && typeDTO.param() instanceof ClassParamDTO param) {
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

}
