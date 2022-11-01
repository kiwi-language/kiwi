package tech.metavm.object.meta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.entity.EntityUtils;
import tech.metavm.object.instance.SQLColumnType;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.persistence.mappers.ConstraintMapper;
import tech.metavm.object.meta.persistence.mappers.FieldMapper;
import tech.metavm.object.meta.persistence.mappers.TypeMapper;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.ChangeList;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

import static tech.metavm.constant.ColumnNames.*;
import static tech.metavm.object.meta.StdTypeConstants.*;

@Component
public class StdTypeManager {

    @Autowired
    private TypeMapper typeMapper;

    @Autowired
    private FieldMapper fieldMapper;

    @Autowired
    private ConstraintMapper constraintMapper;

    @Transactional
    public void initialize() {
        List<TypePO> stdTypes = typeMapper.getStandardTypes();
        List<FieldPO> stdFields = fieldMapper.getStandardFields();
        List<ConstraintPO> stdConstraints = constraintMapper.getStandardConstraints();
        StdTypeContext context = new StdTypeContext(stdTypes, stdFields, stdConstraints);
        for (TypeInfo typeInfo : TYPE_INFO_LIST) {
            initType(typeInfo, context);
        }
        context.getTypeChangeList().apply(
                typeMapper::batchInsert,
                typeMapper::batchUpdate,
                deletes -> typeMapper.batchDelete(NncUtils.map(deletes, TypePO::getId))
        );
        context.getFieldChangeList().apply(
                fieldMapper::batchInsert,
                fieldMapper::batchUpdate,
                deletes -> fieldMapper.batchDelete(NncUtils.map(deletes, FieldPO::getId))
        );
        context.getConstraintChangeList().apply(
                constraintMapper::batchInsert,
                constraintMapper::batchUpdate,
                deletes -> constraintMapper.batchDelete(NncUtils.map(deletes, ConstraintPO::getId))
        );
    }

    private void initType(TypeInfo typeInfo, StdTypeContext context) {
        context.addType(typeInfo.toPO());
        for (FieldInfo field : typeInfo.fields()) {
            context.addField(field.toPO(typeInfo.id()));
        }
        for (UniqueConstraintInfo constraint : typeInfo.uniqueConstraints()) {
            context.addConstraint(constraint.toPO(typeInfo.id()));
        }
        context.addType(typeInfo.getArrayPO());
        context.addType(typeInfo.getNullablePO());
    }

    public static final int MASK_SHIFT = 8;

    public static final TypeInfo[] TYPE_INFO_LIST = new TypeInfo[] {
            TypeInfo.create(OBJECT, "对象", TypeCategory.OBJECT, SQLColumnType.INT64),
            TypeInfo.create(INT, "INT32", TypeCategory.INT, SQLColumnType.INT32),
            TypeInfo.create(LONG, "整数", TypeCategory.LONG, SQLColumnType.INT64),
            TypeInfo.create(DOUBLE, "数值", TypeCategory.DOUBLE, SQLColumnType.FLOAT),
            TypeInfo.create(BOOL, "是否", TypeCategory.BOOL, SQLColumnType.BOOL),
            TypeInfo.create(STRING, "文本", TypeCategory.STRING, SQLColumnType.VARCHAR64),
            TypeInfo.create(TIME, "时间", TypeCategory.TIME, SQLColumnType.INT64),
            TypeInfo.create(DATE, "日期", TypeCategory.DATE, SQLColumnType.INT64),
            TypeInfo.create(ARRAY, "数组", TypeCategory.ARRAY, SQLColumnType.INT64),
            TypeInfo.create(NULLABLE, "可空", TypeCategory.NULLABLE, SQLColumnType.INT64),
            TypeInfo.create(ROLE.ID, "角色", TypeCategory.CLASS, SQLColumnType.INT64,
                    List.of(
                            FieldInfo.createTitle(ROLE.FID_NAME, "名称", S0)
                    )
            ),
            TypeInfo.create(USER.ID, "用户", TypeCategory.CLASS, SQLColumnType.INT64,
                    List.of(
                            FieldInfo.createUniqueString(USER.FID_LOGIN_NAME, "账号", S0),
                            FieldInfo.createTitle(USER.FID_NAME, "名称", S1),
                            FieldInfo.createString(USER.FID_PASSWORD, "密码", T0),
                            FieldInfo.createReference(USER.FID_ROLES, getArrayId(ROLE.ID), "角色", L0)
                    ),
                    List.of(
                            UniqueConstraintInfo.create(
                                    USER.CID_UNIQUE_LOGIN_NAME,
                                    List.of(USER.FID_LOGIN_NAME)
                            )
                    )
            ),
    };

    public static long getNullableId(long id) {
        return 1L << MASK_SHIFT | id;
    }

    public static long getArrayId(long id) {
        return 2L << MASK_SHIFT | id;
    }

    public static boolean isStandardTypeId(long id) {
        return getTypeInfo(id) != null;
    }

    public static TypeDTO getTypeDTO(long id) {
        return NncUtils.get(getTypeInfo(id), StdTypeManager::convertToTypeDTO);
    }

    public static TypePO getTypePO(long id) {
        return NncUtils.get(getTypeInfo(id), StdTypeManager::convertToTypePO);
    }

    public static List<FieldPO> getFieldPOs(long typeId) {
        return NncUtils.map(getTypeInfo(typeId).fields(), f -> convertToFieldPO(typeId, f));
    }

    public static List<ConstraintPO> getConstraintPOs(long typeId) {
        return NncUtils.map(getTypeInfo(typeId).uniqueConstraints(), c -> convertToConstraintPO(typeId, c));
    }

    public static SQLColumnType getSQLColumnType(long id) {
        return NncUtils.get(getTypeInfo(id), TypeInfo::sqlColumnType);
    }

    private static TypeInfo getTypeInfo(long id) {
        return NncUtils.find(TYPE_INFO_LIST, t -> t.id() == id);
    }

    private static TypeDTO convertToTypeDTO(TypeInfo info) {
        return   new TypeDTO(
                info.id(),
                info.name(),
                info.category().code(),
                false,
                false,
                null,
                List.of(),
                null,
                NncUtils.map(info.fields(), f -> convertToFieldDTO(info.id(), f)),
                NncUtils.map(info.uniqueConstraints(), c -> convertToConstraintDTO(info.id(), c)),
                List.of()
        );
    }

    private static TypePO convertToTypePO(TypeInfo info) {
        return new TypePO(
                info.id(),
                -1L,
                info.name(),
                info.category().code(),
                null,
                false,
                false,
                null,
                null
        );
    }

    private static FieldDTO convertToFieldDTO(long ownerId, FieldInfo info) {
        return new FieldDTO(
                info.id(),
                info.name(),
                Access.GLOBAL.code(),
                null,
                info.unique(),
                info.asTitle(),
                ownerId,
                info.typeId(),
                null
        );
    }

    private static FieldPO convertToFieldPO(long declaringTypeId, FieldInfo info) {
        return new FieldPO(
                info.id(),
                -1L,
                info.name(),
                declaringTypeId,
                Access.GLOBAL.code(),
                info.unique(),
                null,
                info.columnName(),
                info.asTitle(),
                info.typeId()
        );
    }

    private static ConstraintPO convertToConstraintPO(long typeId, UniqueConstraintInfo info) {
        return new ConstraintPO(
                info.id(),
                typeId,
                ConstraintKind.UNIQUE.code(),
                NncUtils.toJSONString(new UniqueConstraintParam(info.fieldIds()))
        );
    }

    private static ConstraintDTO convertToConstraintDTO(long typeId, UniqueConstraintInfo info) {
        return new ConstraintDTO(
                info.id(),
                ConstraintKind.UNIQUE.code(),
                typeId,
                new UniqueConstraintParam(info.fieldIds())
        );
    }

    private static void checkStdId(long id) {
        long maxId = (1 << (MASK_SHIFT + 2)) - 1;
        if(id > maxId) {
            throw new InternalException("Standard id(" + id + ") exceeds maximum(" + maxId + ")");
        }
    }

    private static class StdTypeContext {
        private final List<TypePO> existingTypes;
        private final List<FieldPO> existingFields;
        private final List<ConstraintPO> existingConstraints;

        private final List<TypePO> types = new ArrayList<>();
        private final List<FieldPO> fields = new ArrayList<>();
        private final List<ConstraintPO> constraints = new ArrayList<>();

        public StdTypeContext(
                List<TypePO> existingTypes,
                List<FieldPO> existingFields,
                List<ConstraintPO> existingConstraints
        ) {
            this.existingTypes = new ArrayList<>(existingTypes);
            this.existingFields = new ArrayList<>(existingFields);
            this.existingConstraints = new ArrayList<>(existingConstraints);
        }

        public ChangeList<TypePO> getTypeChangeList() {
            return ChangeList.build(existingTypes, types, TypePO::getId, EntityUtils::pojoEquals);
        }

        public ChangeList<FieldPO> getFieldChangeList() {
            return ChangeList.build(existingFields, fields, FieldPO::getId, EntityUtils::pojoEquals);
        }

        public ChangeList<ConstraintPO> getConstraintChangeList() {
            return ChangeList.build(existingConstraints, constraints, ConstraintPO::getId, EntityUtils::pojoEquals);
        }

        public void addType(TypePO typePO) {
            checkStdId(typePO.getId());
            types.add(typePO);
        }

        public void addField(FieldPO fieldPO) {
            checkStdId(fieldPO.getId());
            fields.add(fieldPO);
        }

        public void addConstraint(ConstraintPO constraintPO) {
            checkStdId(constraintPO.getId());
            constraints.add(constraintPO);
        }

    }

}
