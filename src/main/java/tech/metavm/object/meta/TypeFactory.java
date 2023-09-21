package tech.metavm.object.meta;

import tech.metavm.autograph.FlowFactory;
import tech.metavm.autograph.ParamInfo;
import tech.metavm.entity.Entity;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceFactory;
import tech.metavm.flow.Flow;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.rest.dto.ClassParamDTO;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Null;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static tech.metavm.object.meta.TypeUtil.getArrayType;

public class TypeFactory {

    private final Function<Class<?>, Type> getTypeFunc;

    public TypeFactory(Function<Class<?>, Type> getTypeFunc) {
        this.getTypeFunc = getTypeFunc;
    }

    @SuppressWarnings("unused")
    public PrimitiveType createPrimitiveWithComposition(PrimitiveKind kind) {
        PrimitiveType primitiveType = createPrimitive(kind);
        TypeUtil.fillCompositeTypes(primitiveType, this);
        return primitiveType;
    }

    public PrimitiveType createPrimitive(PrimitiveKind kind) {
        return new PrimitiveType(kind);
    }

    public ClassType createAndBind(TypeDTO typeDTO, boolean withContent, IEntityContext context) {
        ClassParamDTO param = (ClassParamDTO) typeDTO.param();
        ClassType type = ClassBuilder.newBuilder(typeDTO.name(), typeDTO.code())
                .tmpId(typeDTO.tmpId())
                .superType(NncUtils.get(param.superTypeId(), context::getClassType))
                .interfaces(NncUtils.map(param.interfaceRefs(), context::getClassType))
                .category(TypeCategory.getByCode(typeDTO.category()))
                .source(ClassSource.getByCode(param.source()))
                .anonymous(typeDTO.anonymous())
                .ephemeral(typeDTO.ephemeral())
                .template(param.template())
                .typeArguments(NncUtils.map(param.typeArgumentRefs(), context::getType))
                .desc(param.desc())
                .build();
        context.bind(type);
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

    public boolean isNullable(Type type) {
        return type.isNullable();
    }

    public Field createField(ClassType declaringType, FieldDTO fieldDTO, IEntityContext context) {
        Type fieldType = context.getType(fieldDTO.typeRef());
        Field field = new Field(
                fieldDTO.name(),
                fieldDTO.code(),
                declaringType,
                fieldType, Access.getByCodeRequired(fieldDTO.access()),
                fieldDTO.unique(),
                fieldDTO.asTitle(),
                InstanceFactory.resolveValue(fieldDTO.defaultValue(), fieldType, context),
                fieldDTO.isChild(),
                fieldDTO.isStatic(),
                InstanceUtils.nullInstance()
        );
        field.setTmpId(fieldDTO.tmpId());
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
        var nullableType = createUnion(Set.of(type, getTypeFunc.apply(Null.class)));
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
        var arrayType = new ArrayType(elementType, true);
        if (typeDTO != null) {
            arrayType.setTmpId(typeDTO.tmpId());
        }
//        createArrayFlows(arrayType, NncUtils.get(typeDTO, TypeDTO::flows));
        return arrayType;
    }

//    private void createArrayFlows(ArrayType arrayType, @Nullable List<FlowDTO> flowDTOs) {
//        var elementType = arrayType.getElementType();
//        Map<String, FlowDTO> flowDTOMap = new HashMap<>();
//        if (flowDTOs != null) {
//            flowDTOMap.putAll(NncUtils.toMap(flowDTOs, FlowDTO::code));
//        }
//        FlowFactory.create(
//                arrayType, getNullType(), "创建", "init", true,
//                false, false, null,
//                flowDTOMap.get("init"),
//                arrayType,
//                new ParamInfo("元素作为子对象", "elementAsChild", getBooleanType())
//        );
//        FlowFactory.create(
//                arrayType, getNullType(), "添加元素", "add", false,
//                false, false, null,
//                flowDTOMap.get("add"),
//                getVoidType(),
//                new ParamInfo("元素", "element", elementType)
//        );
//        FlowFactory.create(
//                arrayType, getNullType(), "删除元素", "remove", false,
//                false, false, null,
//                flowDTOMap.get("remove"),
//                getBooleanType(),
//                new ParamInfo("元素", "element", elementType)
//        );
//        FlowFactory.create(
//                arrayType, getNullType(), "获取元素", "get", false,
//                false, false, null,
//                flowDTOMap.get("get"),
//                elementType,
//                new ParamInfo("位置", "index", getLongType())
//        );
//        FlowFactory.create(
//                arrayType, getNullType(),
//                "设置元素", "set", false,
//                false, false, null,
//                flowDTOMap.get("set"),
//                elementType,
//                new ParamInfo("位置", "index", getLongType()),
//                new ParamInfo("元素", "element", elementType)
//        );
//        FlowFactory.create(
//                arrayType, getNullType(),
//                "删除指定位置元素", "removeAt", false,
//                false, false, null,
//                flowDTOMap.get("removeAt"),
//                elementType,
//                new ParamInfo("位置", "index", getLongType())
//        );
//        FlowFactory.create(
//                arrayType, getNullType(), "计数", "size", false,
//                false, false, null,
//                flowDTOMap.get("size"),
//                getLongType()
//        );
//        FlowFactory.create(
//                arrayType, getNullType(), "清空", "clear", false,
//                false, false, null,
//                flowDTOMap.get("clear"),
//                getVoidType()
//        );
//    }

    public ClassType createIteratorType(Type elementType, TypeSource source, @Nullable TypeDTO typeDTO) {
        String name = "迭代器<" + elementType.getName() + ">";
        String code = elementType.getCode() != null ? "Iterator<" + elementType.getCode() + ">" : null;
        ClassType iteratorType = ClassBuilder.newBuilder(name, code)
                .tmpId(NncUtils.get(typeDTO, TypeDTO::tmpId))
                .template("Iterator")
                .typeArguments(List.of(elementType))
                .category(TypeCategory.INTERFACE).build();
        createIteratorFlows(iteratorType, elementType, typeDTO);
        source.addType(iteratorType);
        return iteratorType;
    }

    private void createIteratorFlows(ClassType iteratorType, Type elementType,
                                     @Nullable TypeDTO typeDTO) {
        var flowDTOMap = getFlowDTOMap(typeDTO);
        boolean isAbstract = iteratorType.isInterface();
        boolean isNative = !iteratorType.isInterface();
        FlowFactory.create(
                iteratorType, getNullType(),
                "是否存在次项", "hasNext",
                false, isAbstract, isNative, null,
                flowDTOMap.get("done"),
                getBooleanType()
        );
        FlowFactory.create(
                iteratorType, getNullType(),
                "获取次项", "next",
                false, isAbstract, isNative, null,
                flowDTOMap.get("next"),
                elementType
        );
    }

    private Map<String, FlowDTO> getFlowDTOMap(@Nullable TypeDTO typeDTO) {
        Map<String, FlowDTO> flowDTOMap = new HashMap<>();
        if (typeDTO != null && typeDTO.param() instanceof ClassParamDTO param) {
            flowDTOMap.putAll(NncUtils.toMap(param.flows(), FlowDTO::code));
        }
        return flowDTOMap;
    }

    public ClassType createCollectionType(Type elementType, TypeSource source, @Nullable TypeDTO typeDTO) {
        String name = "Collection<" + elementType.getName() + ">";
        String code = elementType.getCode() != null ? "Collection<" + elementType.getCode() + ">" : null;
        ClassType collectionType = ClassBuilder.newBuilder(name, code)
                .tmpId(NncUtils.get(typeDTO, TypeDTO::tmpId))
                .template("Collection")
                .typeArguments(List.of(elementType))
                .category(TypeCategory.INTERFACE)
                .build();
        var iteratorType = TypeUtil.getIteratorType(elementType, source, this);
        createCollectionFlows(collectionType, iteratorType, elementType, typeDTO);
        source.addType(collectionType);
        return collectionType;
    }

    private void createCollectionFlows(ClassType collectionType, ClassType iteratorType,
                                       Type elememtType, @Nullable TypeDTO typeDTO) {
        var flowDTOMap = getFlowDTOMap(typeDTO);
        FlowFactory.create(
                collectionType, getNullType(), "获取迭代器",
                "iterator", false, true, false, null,
                flowDTOMap.get("iterator"), iteratorType
        );
        FlowFactory.create(
                collectionType, getNullType(), "计数",
                "size", false, true, false, null,
                flowDTOMap.get("size"), getLongType()
        );
        FlowFactory.create(
                collectionType, getNullType(), "是否为空",
                "isEmpty", false, true, false, null,
                flowDTOMap.get("isEmpty"), getBooleanType()
        );
        FlowFactory.create(
                collectionType, getNullType(), "是否包含",
                "contains", false, true, false, null,
                flowDTOMap.get("contains"), getBooleanType(),
                new ParamInfo("元素", "element", elememtType)
        );
        FlowFactory.create(
                collectionType, getNullType(), "添加",
                "add", false, true, false, null,
                flowDTOMap.get("add"), getBooleanType(),
                new ParamInfo("元素", "element", elememtType)
        );
        FlowFactory.create(
                collectionType, getNullType(), "删除",
                "remove", false, true, false, null,
                flowDTOMap.get("remove"), getBooleanType(),
                new ParamInfo("元素", "element", elememtType)
        );
        FlowFactory.create(
                collectionType, getNullType(), "清空",
                "clear", false, true, false, null,
                flowDTOMap.get("clear"), getVoidType()
        );
    }

    public ClassType createSetType(Type elementType, TypeSource source, @Nullable TypeDTO typeDTO) {
        String name = "集合<" + elementType.getName() + ">";
        String code = elementType.getCode() != null ? "Set<" + elementType.getCode() + ">" : null;
        var collectionType = TypeUtil.getCollectionType(elementType, source, this);
        TypeUtil.getIteratorImplType(elementType, source, this);
        ClassType setType = ClassBuilder.newBuilder(name, code)
                .tmpId(NncUtils.get(typeDTO, TypeDTO::tmpId))
                .interfaces(List.of(collectionType))
                .ephemeral(true)
                .typeArguments(List.of(elementType))
                .template("Set")
                .build();
        FieldBuilder.newBuilder("数组", "array", setType, TypeUtil.getArrayType(elementType))
                .nullType(getNullType())
                .access(Access.CLASS)
                .isChild(true).build();
        createSetFlows(setType, collectionType, typeDTO);
        source.addType(setType);
        return setType;
    }

    private void createSetFlows(ClassType setType, ClassType collectionType, @Nullable TypeDTO typeDTO) {
        var flowDTOMap = getFlowDTOMap(typeDTO);
        FlowFactory.create(
                setType, getNullType(), "初始化", "init",
                true, false, true, null,
                flowDTOMap.get("init"), setType,
                new ParamInfo("元素作为从对象", "elementAsChild", getBooleanType())
        );
        createOverridingFlows(setType, collectionType, flowDTOMap);
    }

    public ClassType createListType(Type elementType, IEntityContext context, @Nullable TypeDTO typeDTO) {
        return createListType(elementType, new ContextTypeSource(context), typeDTO);
    }

    public ClassType createListType(Type elementType, TypeSource source, @Nullable TypeDTO typeDTO) {
        String name = "列表<" + elementType.getName() + ">";
        String code = elementType.getCode() != null ? "List<" + elementType.getCode() + ">" : null;
        var collectionType = TypeUtil.getCollectionType(elementType, source, this);
        TypeUtil.getIteratorImplType(elementType, source, this);
        ClassType listType = ClassBuilder.newBuilder(name, code)
                .tmpId(NncUtils.get(typeDTO, TypeDTO::tmpId))
                .interfaces(List.of(collectionType))
                .ephemeral(true)
                .template("List")
                .typeArguments(List.of(elementType))
                .build();
        FieldBuilder.newBuilder("数组", "array", listType, TypeUtil.getArrayType(elementType))
                .nullType(getNullType())
                .access(Access.CLASS)
                .isChild(true).build();
        createListFlows(listType, collectionType, typeDTO);
        source.addType(listType);
        return listType;
    }

    private void createListFlows(ClassType listType, ClassType collectionType, @Nullable TypeDTO typeDTO) {
        var flowDTOMap = getFlowDTOMap(typeDTO);
        FlowFactory.create(
                listType, getNullType(), "初始化", "init",
                true, false, true, null,
                flowDTOMap.get("init"), listType,
                new ParamInfo("元素作为从对象", "elementAsChild", getBooleanType())
        );
        createOverridingFlows(listType, collectionType, flowDTOMap);
    }

    public ClassType createIteratorImplType(Type elementType, TypeSource source, @Nullable TypeDTO typeDTO) {
        String name = "迭代器实现<" + elementType.getName() + ">";
        String code = elementType.getCode() != null ? "IteratorImpl<" + elementType.getCode() + ">" : null;
        var iteratorType = TypeUtil.getIteratorType(elementType, source, this);
        ClassType iteratorImplType = ClassBuilder.newBuilder(name, code)
                .tmpId(NncUtils.get(typeDTO, TypeDTO::tmpId))
                .interfaces(List.of(iteratorType))
                .ephemeral(true)
                .template("IteratorImpl")
                .typeArguments(List.of(elementType))
                .build();
        Map<String, FlowDTO> flowDTOMap = getFlowDTOMap(typeDTO);
        FlowFactory.create(
                iteratorImplType, getNullType(), "初始化",
                "init", true, false, true, null,
                flowDTOMap.get("init"),
                iteratorImplType,
                new ParamInfo("集合", "collection",
                        TypeUtil.getCollectionType(elementType, source, this))
        );
        createOverridingFlows(iteratorImplType, iteratorType, flowDTOMap);
        source.addType(iteratorImplType);
        return iteratorImplType;
    }

    private void createOverridingFlows(ClassType declaringType, ClassType baseType, Map<String, FlowDTO> flowDTOMap) {
        for (Flow flow : baseType.getFlows()) {
            FlowFactory.create(
                    declaringType, getNullType(), flow.getName(), flow.getCode(),
                    false, false, true, flow,
                    flowDTOMap.get(flow.getCode()),
                    flow.getOutputType(),
                    NncUtils.map(
                            flow.getInputType().getFields(),
                            field -> new ParamInfo(
                                    field.getName(), field.getCode(),
                                    field.getType()
                            )
                    )
            );
        }
    }

    public ClassType createMapType(Type keyType, Type valueType, IEntityContext context, @Nullable TypeDTO typeDTO) {
        String name = "词典<" + keyType.getName() + "," + valueType.getName() + ">";
        String code = keyType.getCode() != null && valueType.getCode() != null ?
                "Map<" + keyType.getCode() + "," + valueType.getCode() + ">" : null;
        ClassType mapType = ClassBuilder.newBuilder(name, code)
                .template("Map")
                .tmpId(NncUtils.get(typeDTO, TypeDTO::tmpId))
                .ephemeral(true)
                .typeArguments(List.of(keyType, valueType))
                .build();
        FieldBuilder
                .newBuilder("键数组", "keyArray", mapType, getArrayType(keyType, this))
                .access(Access.CLASS)
                .nullType(getNullType())
                .isChild(true)
                .build();
        FieldBuilder
                .newBuilder("值数组", "valueArray", mapType, getArrayType(valueType, this))
                .access(Access.CLASS)
                .nullType(getNullType())
                .isChild(true)
                .build();
        createMapFlows(mapType, keyType, valueType, typeDTO);
        context.bind(mapType);
        return mapType;
    }

    private void createMapFlows(ClassType mapType, Type keyType, Type valueType, @Nullable TypeDTO typeDTO) {
        Map<String, FlowDTO> flowDTOMap = getFlowDTOMap(typeDTO);
        FlowFactory.create(
                mapType, getNullType(), "初始化", "init", true,
                false, true, null,
                flowDTOMap.get("init"),
                mapType,
                new ParamInfo("键作为子对象", "keyAsChild", getBooleanType()),
                new ParamInfo("值作为子对象", "valueAsChild", getBooleanType())
        );
        FlowFactory.create(
                mapType, getNullType(),
                "添加", "put", false,
                false, true, null,
                flowDTOMap.get("put"),
                TypeUtil.getNullableType(valueType),
                new ParamInfo("键", "key", keyType),
                new ParamInfo("值", "value", valueType)
        );
        FlowFactory.create(
                mapType, getNullType(), "查询", "get", false,
                false, true, null,
                flowDTOMap.get("get"),
                TypeUtil.getNullableType(valueType),
                new ParamInfo("键", "key", keyType)
        );
        FlowFactory.create(
                mapType, getNullType(), "删除", "remove", false,
                false, true, null,
                flowDTOMap.get("remove"),
                TypeUtil.getNullableType(valueType),
                new ParamInfo("键", "key", keyType)
        );
        FlowFactory.create(
                mapType, getNullType(), "计数", "size", false,
                false, true, null,
                flowDTOMap.get("size"),
                getLongType()
        );
        FlowFactory.create(
                mapType, getNullType(), "清空", "clear", false,
                false, true, null,
                flowDTOMap.get("clear"),
                getVoidType()
        );
    }

    public PrimitiveType getNullType() {
        return (PrimitiveType) getTypeFunc.apply(Null.class);
    }

    public PrimitiveType getVoidType() {
        return (PrimitiveType) getTypeFunc.apply(Void.class);
    }

    public PrimitiveType getLongType() {
        return (PrimitiveType) getTypeFunc.apply(Long.class);
    }


    @SuppressWarnings("unused")
    public ObjectType getObjectType() {
        return (ObjectType) getTypeFunc.apply(Object.class);
    }

    public ClassType getEntityType() {
        return (ClassType) getTypeFunc.apply(Entity.class);
    }

    public UnionType createUnion(Set<Type> types) {
        return new UnionType(types);
    }

    public PrimitiveType getBooleanType() {
        return (PrimitiveType) getTypeFunc.apply(Boolean.class);
    }

}
