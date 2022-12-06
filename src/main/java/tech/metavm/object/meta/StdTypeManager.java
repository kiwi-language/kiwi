//package tech.metavm.object.meta;
//
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import tech.metavm.entity.EntityUtils;
//import tech.metavm.object.instance.SQLColumnType;
//import tech.metavm.object.meta.persistence.ConstraintPO;
//import tech.metavm.object.meta.persistence.FieldPO;
//import tech.metavm.object.meta.persistence.TypePO;
//import tech.metavm.object.meta.persistence.mappers.ConstraintMapper;
//import tech.metavm.object.meta.persistence.mappers.FieldMapper;
//import tech.metavm.object.meta.persistence.mappers.TypeMapper;
//import tech.metavm.object.meta.rest.dto.ConstraintDTO;
//import tech.metavm.object.meta.rest.dto.FieldDTO;
//import tech.metavm.object.meta.rest.dto.TypeDTO;
//import tech.metavm.util.ChangeList;
//import tech.metavm.util.InternalException;
//import tech.metavm.util.NncUtils;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import static tech.metavm.constant.ColumnNames.*;
//import static tech.metavm.object.meta.IdConstants.*;
//
//@Component
//public class StdTypeManager {
//
//    private final TypeMapper typeMapper;
//    private final FieldMapper fieldMapper;
//    private final ConstraintMapper constraintMapper;
//
//    public StdTypeManager(TypeMapper typeMapper, FieldMapper fieldMapper, ConstraintMapper constraintMapper) {
//        this.typeMapper = typeMapper;
//        this.fieldMapper = fieldMapper;
//        this.constraintMapper = constraintMapper;
//    }
//
//    @Transactional
//    public void initialize() {
//        List<TypePO> stdTypes = typeMapper.getStandardTypes();
//        List<FieldPO> stdFields = fieldMapper.getStandardFields();
//        List<ConstraintPO> stdConstraints = constraintMapper.getStandardConstraints();
//        StdTypeContext context = new StdTypeContext(stdTypes, stdFields, stdConstraints);
//        for (TypeInfo typeInfo : TYPE_INFO_LIST) {
//            initType(typeInfo, context);
//        }
//        context.getTypeChangeList().apply(
//                typeMapper::batchInsert,
//                typeMapper::batchUpdate,
//                deletes -> typeMapper.batchDelete(NncUtils.map(deletes, TypePO::getId))
//        );
//        context.getFieldChangeList().apply(
//                fieldMapper::batchInsert,
//                fieldMapper::batchUpdate,
//                deletes -> fieldMapper.batchDelete(NncUtils.map(deletes, FieldPO::getId))
//        );
//        context.getConstraintChangeList().apply(
//                constraintMapper::batchInsert,
//                constraintMapper::batchUpdate,
//                deletes -> constraintMapper.batchDelete(NncUtils.map(deletes, ConstraintPO::getId))
//        );
//    }
//
//    private void initType(TypeInfo typeInfo, StdTypeContext context) {
//        context.addType(typeInfo.toPO());
//        for (FieldInfo field : typeInfo.fields()) {
//            context.addField(field.toPO(typeInfo.id()));
//        }
//        for (UniqueConstraintInfo constraint : typeInfo.uniqueConstraints()) {
//            context.addConstraint(constraint.toPO(typeInfo.id()));
//        }
//        if(typeInfo.category() != TypeCategory.ARRAY) {
//            context.addType(typeInfo.getArrayPO());
//        }
//        if(typeInfo.category() != TypeCategory.NULLABLE && typeInfo.category() != TypeCategory.ARRAY) {
//            context.addType(typeInfo.getNullablePO());
//        }
//    }
//
//    public static final TypeInfo[] TYPE_INFO_LIST = new TypeInfo[] {
//            TypeInfo.createClass(OBJECT, "对象"),
//            TypeInfo.create(INT, "INT32", TypeCategory.INT, SQLColumnType.INT32),
//            TypeInfo.create(LONG, "整数", TypeCategory.LONG, SQLColumnType.INT64),
//            TypeInfo.create(DOUBLE, "数值", TypeCategory.DOUBLE, SQLColumnType.FLOAT),
//            TypeInfo.create(BOOL, "是否", TypeCategory.BOOL, SQLColumnType.BOOL),
//            TypeInfo.create(STRING, "文本", TypeCategory.STRING, SQLColumnType.VARCHAR64),
//            TypeInfo.create(TIME, "时间", TypeCategory.TIME, SQLColumnType.INT64),
//            TypeInfo.create(DATE, "日期", TypeCategory.DATE, SQLColumnType.INT64),
//            TypeInfo.create(PASSWORD, "密码", TypeCategory.PRIMITIVE, SQLColumnType.VARCHAR64),
//            TypeInfo.create(ARRAY, "数组", TypeCategory.ARRAY, SQLColumnType.INT64),
//            TypeInfo.create(NULL, "空", TypeCategory.NULL, SQLColumnType.INT64),
//            TypeInfo.createClass(TENANT.ID, "租户"),
//            TypeInfo.createClass(TYPE.ID, "类型"),
//            TypeInfo.createClass(FIELD.ID, "字段"),
//            TypeInfo.createClass(UNIQUE_CONSTRAINT.ID, "唯一约束"),
//            TypeInfo.createClass(CHECK_CONSTRAINT.ID, "校验约束"),
//            TypeInfo.createClass(FLOW.ID, "流程"),
//            TypeInfo.createClass(SCOPE.ID, "范围"),
//            TypeInfo.createClass(ADD_OBJECT_NODE.ID, "新增对象节点"),
//            TypeInfo.createClass(UPDATE_OBJECT_NODE.ID, "更新对象节点"),
//            TypeInfo.createClass(DELETE_OBJECT_NODE.ID, "删除对象节点"),
//            TypeInfo.createClass(GET_OBJECT_NODE.ID, "查询对象节点"),
//            TypeInfo.createClass(GET_RELATION_NODE.ID, "查询关联对象节点"),
//            TypeInfo.createClass(DIRECTORY_ACCESS_NODE.ID, "查询目录节点"),
//            TypeInfo.createClass(GET_UNIQUE_NODE.ID, "查询索引节点"),
//            TypeInfo.createClass(SELF_NODE.ID, "当前对象节点"),
//            TypeInfo.createClass(INPUT_NODE.ID, "输入节点"),
//            TypeInfo.createClass(RETURN_NODE.ID, "结束节点"),
//            TypeInfo.createClass(EXCEPTION_NODE.ID, "异常节点"),
//            TypeInfo.createClass(SUB_FLOW_NODE.ID, "子流程节点"),
//            TypeInfo.createClass(LOOP_NODE.ID, "循环节点"),
//            TypeInfo.createClass(BRANCH_NODE.ID, "分支节点"),
//
//            TypeInfo.createClass(ROLE.ID, "角色",
//                    List.of(
//                            FieldInfo.createTitle(ROLE.FID_NAME, "名称", S0)
//                    )
//            ),
//
//            TypeInfo.createClass(USER.ID, "用户",
//                    List.of(
//                            FieldInfo.createUniqueString(USER.FID_LOGIN_NAME, "账号", S0),
//                            FieldInfo.createTitle(USER.FID_NAME, "名称", S1),
//                            FieldInfo.createPassword(USER.FID_PASSWORD, "密码", S2),
//                            FieldInfo.createReference(USER.FID_ROLES, getArrayId(ROLE.ID), "角色", L0)
//                    ),
//                    List.of(
//                            UniqueConstraintInfo.create(
//                                    USER.CID_UNIQUE_LOGIN_NAME,
//                                    List.of(
//                                            UniqueConstraintItemInfo.createForField("账号", USER.FID_LOGIN_NAME)
//                                    ),
//                                    "账号重复"
//                            )
//                    )
//            ),
//    };
//
//    public static long getNullableId(long id) {
//        return id - TYPE_BASE + NULLABLE_TYPE_BASE;
//    }
//
//    public static long getArrayId(long id) {
//        return id - TYPE_BASE + ARRAY_TYPE_BASE;
//    }
//
//    public static long getUniqueConstraintId(long fieldId) {
//        return fieldId - FIELD_BASE + UNIQUE_CONSTRAINT_BASE;
//    }
//
//    public static boolean isStandardTypeId(long id) {
//        return getTypeInfo(id) != null;
//    }
//
//    @SuppressWarnings("unused")
//    public static FieldPO getFieldPO(long id) {
//        for (TypeInfo typeInfo : TYPE_INFO_LIST) {
//            for (FieldInfo field : typeInfo.fields()) {
//                if(field.id() == id) {
//                    return convertToFieldPO(typeInfo.id(), field);
//                }
//            }
//        }
//        throw new InternalException("Can not found standard field with id: " + id);
//    }
//
//    public static List<FieldPO> getFieldPOs(List<Long> ids) {
//        Set<Long> idSet = new HashSet<>(ids);
//        List<FieldPO> results = new ArrayList<>();
//        for (TypeInfo typeInfo : TYPE_INFO_LIST) {
//            for (FieldInfo field : typeInfo.fields()) {
//                if(idSet.contains(field.id())) {
//                    idSet.remove(field.id());
//                    results.add(convertToFieldPO(typeInfo.id(), field));
//                }
//            }
//        }
//        if(!idSet.isEmpty()) {
//            throw new InternalException("Can not found standard field for ids: " + idSet);
//        }
//        return results;
//    }
//
//    @SuppressWarnings("unused")
//    public static boolean isStandardFieldId(long id) {
//        for (TypeInfo typeInfo : TYPE_INFO_LIST) {
//            for (FieldInfo field : typeInfo.fields()) {
//                if(field.id() == id) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    @SuppressWarnings("unused")
//    public static TypeDTO getTypeDTO(long id) {
//        return NncUtils.get(getTypeInfo(id), StdTypeManager::convertToTypeDTO);
//    }
//
//    public static TypePO getTypePO(long id) {
//        return NncUtils.get(getTypeInfo(id), StdTypeManager::convertToTypePO);
//    }
//
//    public static List<FieldPO> getFieldPOsByTypeId(long typeId) {
//        return NncUtils.map(getTypeInfo(typeId).fields(), f -> convertToFieldPO(typeId, f));
//    }
//
//    public static List<FieldPO> getFieldPOsByTypeIds(List<Long> typeIds) {
//        return NncUtils.flatMap(typeIds, StdTypeManager::getFieldPOsByTypeId);
//    }
//
//    @SuppressWarnings("unused")
//    public static List<ConstraintPO> getConstraintPOs(long typeId) {
//        return NncUtils.map(getTypeInfo(typeId).uniqueConstraints(), c -> convertToConstraintPO(typeId, c));
//    }
//
//    public static SQLColumnType getSQLColumnType(long id) {
//        return NncUtils.get(getTypeInfo(id), TypeInfo::sqlColumnType);
//    }
//
//    private static TypeInfo getTypeInfo(long id) {
//        return NncUtils.find(TYPE_INFO_LIST, t -> t.id() == id);
//    }
//
//    private static TypeDTO convertToTypeDTO(TypeInfo info) {
//        return TypeDTO.create(
//                info.id(),
//                info.name(),
//                OBJECT,
//                info.category().code(),
//                false,
//                false,
//                null,
//                null,
//                List.of(),
//                null,
//                null,
//                null,
//                NncUtils.map(info.fields(), f -> convertToFieldDTO(info.id(), f)),
//                NncUtils.map(info.uniqueConstraints(), c -> convertToConstraintDTO(info.id(), c)),
//                List.of()
//        );
//    }
//
//    private static TypePO convertToTypePO(TypeInfo info) {
//        return new TypePO(
//                info.id(),
//                -1L,
//                OBJECT,
//                info.name(),
//                info.category().code(),
//                null,
//                false,
//                false,
//                null,
//                null,
//                null
//        );
//    }
//
//    private static FieldDTO convertToFieldDTO(long ownerId, FieldInfo info) {
//        return new FieldDTO(
//                info.id(),
//                info.name(),
//                Access.GLOBAL.code(),
//                null,
//                info.unique(),
//                info.asTitle(),
//                ownerId,
//                info.typeId(),
//                null,
//                info.isChild()
//        );
//    }
//
//    private static FieldPO convertToFieldPO(long declaringTypeId, FieldInfo info) {
//        return new FieldPO(
//                info.id(),
//                -1L,
//                info.name(),
//                declaringTypeId,
//                Access.GLOBAL.code(),
//                info.unique(),
//                null,
//                info.columnName(),
//                info.asTitle(),
//                info.typeId()
//        );
//    }
//
//    private static ConstraintPO convertToConstraintPO(long typeId, UniqueConstraintInfo info) {
//        return new ConstraintPO(
//                info.id(),
//                typeId,
//                ConstraintKind.UNIQUE.code(),
//                info.message(),
//                NncUtils.toJSONString(info.getParam())
//        );
//    }
//
//    private static ConstraintDTO convertToConstraintDTO(long typeId, UniqueConstraintInfo info) {
//        return new ConstraintDTO(
//                info.id(),
//                ConstraintKind.UNIQUE.code(),
//                typeId,
//                info.message(),
//                info.getParam()
//        );
//    }
//
//    private static void checkStdId(long id) {
//        long maxId = CLASS_REGION_BASE + SYSTEM_RESERVE_PER_REGION;
//        if(id > maxId) {
//            throw new InternalException("Standard id(" + id + ") exceeds maximum(" + maxId + ")");
//        }
//    }
//
//    private static class StdTypeContext {
//        private final List<TypePO> existingTypes;
//        private final List<FieldPO> existingFields;
//        private final List<ConstraintPO> existingConstraints;
//
//        private final List<TypePO> types = new ArrayList<>();
//        private final List<FieldPO> fields = new ArrayList<>();
//        private final List<ConstraintPO> constraints = new ArrayList<>();
//
//        public StdTypeContext(
//                List<TypePO> existingTypes,
//                List<FieldPO> existingFields,
//                List<ConstraintPO> existingConstraints
//        ) {
//            this.existingTypes = NncUtils.sortById(existingTypes);
//            this.existingFields = NncUtils.sortById(existingFields);
//            this.existingConstraints = NncUtils.sortById(existingConstraints);
//        }
//
//        public ChangeList<TypePO> getTypeChangeList() {
//            return ChangeList.build(existingTypes, NncUtils.sortById(types), TypePO::getId, EntityUtils::pojoEquals);
//        }
//
//        public ChangeList<FieldPO> getFieldChangeList() {
//            return ChangeList.build(existingFields, NncUtils.sortById(fields), FieldPO::getId, EntityUtils::pojoEquals);
//        }
//
//        public ChangeList<ConstraintPO> getConstraintChangeList() {
//            return ChangeList.build(existingConstraints, NncUtils.sortById(constraints), ConstraintPO::getId, EntityUtils::pojoEquals);
//        }
//
//        public void addType(TypePO typePO) {
//            checkStdId(typePO.getId());
//            types.add(typePO);
//        }
//
//        public void addField(FieldPO fieldPO) {
//            checkStdId(fieldPO.getId());
//            fields.add(fieldPO);
//        }
//
//        public void addConstraint(ConstraintPO constraintPO) {
//            checkStdId(constraintPO.getId());
//            constraints.add(constraintPO);
//        }
//
//    }
//
//}
