package tech.metavm.object.type;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.common.Page;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.EntityContextFactoryBean;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.rest.ArrayFieldValue;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.ReferenceFieldValue;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Password;

import javax.annotation.Nullable;
import java.util.*;

import static tech.metavm.object.type.Types.*;
import static tech.metavm.util.NncUtils.requireNonNull;

@Component
public class TableManager extends EntityContextFactoryBean {

    private final TypeManager typeManager;


    public TableManager(EntityContextFactory entityContextFactory, TypeManager typeManager) {
        super(entityContextFactory);
        this.typeManager = typeManager;
    }

    public TableDTO get(long id) {
        try (IEntityContext context = newContext()) {
            ClassType type = context.getClassType(id);
            return NncUtils.get(type, Type::toDTO, t -> convertToTable(t, context));
        }
    }

    @Transactional
    public TableDTO save(TableDTO table) {
        IEntityContext context = newContext();
        TypeDTO typeDTO = ClassTypeDTOBuilder.newBuilder(table.name())
                .code(table.code())
                .ephemeral(table.ephemeral())
                .anonymous(table.anonymous())
                .build();
        ClassType type = typeManager.saveType(typeDTO, context);
        context.initIds();
        NncUtils.map(table.fields(), column -> saveField(column, type, context));
        saveTitleField(table.titleField(), type, context);
        context.finish();
        return convertToTable(type.toDTO(), context);
    }

    private void saveTitleField(TitleFieldDTO titleFieldDTO, ClassType type, IEntityContext context) {
        if (titleFieldDTO != null) {
            Field titleField = type.getTitleField();
            if (titleField == null) {
                Type titleFieldType = getType(
                        titleFieldDTO.name(),
                        titleFieldDTO.type(),
                        true,
                        false,
                        null,
                        context
                );
                titleField = FieldBuilder.newBuilder(titleFieldDTO.name(), null, type, titleFieldType)
                        .unique(titleFieldDTO.unique())
                        .build();
                type.setTitleField(titleField);
            } else {
                titleField.setName(titleFieldDTO.name());
                titleField.setUnique(titleFieldDTO.unique());
            }
        }
    }

    @Transactional
    public long saveColumn(ColumnDTO column) {
        requireNonNull(column.ownerId(), () -> BusinessException.invalidParams("表格ID必填"));
        IEntityContext context = newContext();
        ClassType declaringType = context.getClassType(column.ownerId());
        Field field = saveField(column, declaringType, context);
        context.finish();
        return field.getIdRequired();
    }

    public ColumnDTO getColumn(long id) {
        try (var context = newContext()) {
            FieldDTO fieldDTO = context.getField(id).toDTO();
            if (fieldDTO == null || !isVisible(fieldDTO, context)) {
                return null;
            }
            return convertToColumnDTO(fieldDTO, context.getType(fieldDTO.typeId()));
        }
    }

    private EnumEditContext saveEnum(Type declaringType, ColumnDTO fieldEdit, IEntityContext context) {
        EnumEditContext enumEditContext = new EnumEditContext(
                fieldEdit.targetId(),
                declaringType.getName() + "_" + fieldEdit.name(),
                true,
                fieldEdit.choiceOptions(),
                context
        );
        enumEditContext.execute();
        return enumEditContext;
    }

    private Field saveField(ColumnDTO column, Type declaringType, IEntityContext context) {
        Type type;
        FieldValue defaultValue;
        if (column.type() == ColumnType.ENUM.code) {
            EnumEditContext enumEditContext = saveEnum(declaringType, column, context);
            type = getType(column, enumEditContext.getType(), context);
            defaultValue = column.multiValued() ?
                    new ArrayFieldValue(
                            null,
                            false,
                            NncUtils.map(enumEditContext.getDefaultOptions(), enumConstantRT -> enumConstantRT.toFieldValue(context.getInstanceContext()))
                    )
                    : NncUtils.first(enumEditContext.getDefaultOptions(), enumConstantRT1 -> enumConstantRT1.toFieldValue(context.getInstanceContext()));
        } else {
            type = getType(column, NncUtils.get(column.targetId(), context::getType), context);
            defaultValue = column.defaultValue();
        }
        return typeManager.saveField(
                FieldDTOBuilder.newBuilder(column.name(), RefDTO.fromId(type.getId()))
                        .id(column.id())
                        .access(column.access())
                        .defaultValue(defaultValue)
                        .unique(column.unique())
                        .declaringTypeId(declaringType.getId())
                        .build(),
                context
        );
    }

    private ColumnDTO convertToColumnDTO(FieldDTO field, Type type) {
        TypeInfo typeInfo = getTypeInfo(type, field.defaultValue());
        return new ColumnDTO(
                field.id(),
                field.name(),
                typeInfo.columnType.code,
                field.access(),
                field.declaringTypeId(),
                typeInfo.concreteTypeId,
                typeInfo.name,
                typeInfo.required,
                typeInfo.multiValued,
                field.unique(),
                field.defaultValue(),
                typeInfo.choiceOptions
        );
    }

    private TypeInfo getTypeInfo(Type type, FieldValue fieldDefaultValue) {
        Type concreteType = type.getConcreteType();
        return new TypeInfo(
                getColumnType(concreteType),
                concreteType.getName(),
                concreteType.isEnum() || concreteType.isClass() || concreteType.isValue() ?
                        concreteType.getId() : null,
                type.isNotNull(),
                type.isBinaryNullable() ? type.getUnderlyingType().isArray() : type.isArray(),
                getChoiceOptions(concreteType, fieldDefaultValue)
        );
    }

    private List<ChoiceOptionDTO> getChoiceOptions(Type type, FieldValue fieldDefaultValue) {
        if (type instanceof ClassType classType && type.isEnum()) {
            var enumConstants = NncUtils.map(
                    classType.getEnumConstants(),
                    EnumConstantRT::new
            );
            return getChoiceOptions(
                    NncUtils.sortByInt(enumConstants, EnumConstantRT::getOrdinal),
                    fieldDefaultValue
            );
        } else {
            return List.of();
        }
    }

    private ColumnType getColumnType(Type type) {
        if (isLong(type)) {
            return ColumnType.LONG;
        }
        if (isDouble(type)) {
            return ColumnType.DOUBLE;
        }
        if (isBool(type)) {
            return ColumnType.BOOL;
        }
        if (isString(type)) {
            return ColumnType.STRING;
        }
        if (isTime(type)) {
            return ColumnType.TIME;
        }
        if (type.isEnum()) {
            return ColumnType.ENUM;
        }
        if (type instanceof ClassType) {
            return ColumnType.TABLE;
        }
        if (type instanceof AnyType || type instanceof TypeVariable) {
            return ColumnType.ANY;
        }
        throw new InternalException("Can not get column type for type: " + type);
    }

    private List<ChoiceOptionDTO> getChoiceOptions(List<EnumConstantRT> enumConstants, FieldValue fieldDefaultValue) {
        return NncUtils.sortAndMap(
                enumConstants,
                Comparator.comparingInt(EnumConstantRT::getOrdinal),
                ec -> new ChoiceOptionDTO(
                        ec.getId(),
                        ec.getName(),
                        ec.getOrdinal(),
                        isPreselected(ec, fieldDefaultValue)
                )
        );
    }

    private boolean isPreselected(EnumConstantRT enumConstant, FieldValue fieldDefaultValue) {
        if (fieldDefaultValue instanceof ReferenceFieldValue ref) {
            return Objects.equals(ref.getId(), enumConstant.getInstanceIdString());
        }
        if (fieldDefaultValue instanceof ArrayFieldValue array) {
            for (FieldValue element : array.getElements()) {
                if (element instanceof ReferenceFieldValue ref) {
                    if (Objects.equals(ref.getId(), enumConstant.getInstanceIdString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Type getType(ColumnDTO column, Type concreteType, IEntityContext context) {
        return getType(column.name(), column.type(), column.required(), column.multiValued(), concreteType, context);
    }

    private Type getType(String name, int columnTypeCode, boolean required, boolean multiValued,
                         Type concreteType, IEntityContext context) {
        ColumnType columnType = ColumnType.getByCode(columnTypeCode);
        if (concreteType == null && columnType.getType() != null) {
            concreteType = columnType.getType();
        }
        Type type;
        if (concreteType != null) {
            type = concreteType;
            if (multiValued) {
                type = context.getArrayType(type, ArrayKind.READ_WRITE);
            }
            if (!required) {
                type = context.getNullableType(type);
            }
        } else {
            throw BusinessException.invalidColumn(name, "未选择列类型或未选择关联表格");
        }
        if (type.getId() == null) {
            if (!context.containsModel(type)) {
                context.bind(type);
            }
            context.initIds();
        }
        return type;
    }

    public Page<TableDTO> list(String searchText, int page, int pageSize) {
        try (IEntityContext context = newContext()) {
            var request = new TypeQuery(
                    searchText, List.of(TypeCategory.CLASS.code(), TypeCategory.VALUE.code()),
                    false, false, false, null,
                    List.of(),
                    page, pageSize
            );
            Page<TypeDTO> typePage = typeManager.query(request);
            return new Page<>(
                    NncUtils.map(typePage.data(), t -> convertToTable(t, context)),
                    typePage.total()
            );
        }
    }

    private TableDTO convertToTable(TypeDTO typeDTO, IEntityContext context) {
        ClassTypeParam param = (ClassTypeParam) typeDTO.param();
        FieldDTO titleField = param.titleFieldRef() != null ?
                NncUtils.find(param.fields(), f -> f.getRef().equals(param.titleFieldRef())) : null;
        return new TableDTO(
                typeDTO.id(),
                typeDTO.name(),
                typeDTO.code(),
                param.desc(),
                typeDTO.ephemeral(),
                typeDTO.anonymous(),
                NncUtils.get(titleField, f -> convertToTitleField(f, context)),
                NncUtils.filterAndMap(
                        param.fields(),
                        f -> isVisible(f, context),
                        f -> convertToColumnDTO(f, context.getType(f.typeId()))
                )
        );
    }

    private static final Set<Class<?>> CONFIDENTIAL_JAVA_CLASSES = Set.of(Password.class);

    private boolean isVisible(FieldDTO fieldDTO, IEntityContext context) {
        NncUtils.requireNonNull(fieldDTO.typeId(), "字段'" + fieldDTO.name() + "'的typeId为空");
        Type fieldType = context.getType(fieldDTO.typeId());
        if (ModelDefRegistry.containsDef(fieldType)) {
            Class<?> javaClass = ModelDefRegistry.getJavaClass(fieldType);
            return !CONFIDENTIAL_JAVA_CLASSES.contains(javaClass);
        } else {
            return true;
        }
    }

    private TitleFieldDTO convertToTitleField(FieldDTO fieldDTO, IEntityContext context) {
        Type fieldType = context.getType(fieldDTO.typeId());
        return new TitleFieldDTO(
                fieldDTO.name(),
                getColumnType(fieldType.getConcreteType()).code,
                fieldDTO.unique(),
                fieldDTO.defaultValue()
        );
    }

    private record TypeInfo(
            ColumnType columnType,
            String name,
            Long concreteTypeId,
            boolean required,
            boolean multiValued,
            List<ChoiceOptionDTO> choiceOptions
    ) {

    }

    public enum ColumnType {
        STRING(1, String.class),
        DOUBLE(2, Double.class),
        LONG(3, Long.class),
        INT(4, Integer.class),
        BOOL(6, Boolean.class),
        ENUM(7, null),
        TIME(9, Date.class),
        TABLE(10, null),
        DATE(13, Date.class),
        PASSWORD(14, null),
        ANY(15, null),
        ;

        private final int code;

        @Nullable
        private final Class<?> javaType;

        ColumnType(int code, @Nullable Class<?> javaType) {
            this.code = code;
            this.javaType = javaType;
        }

        public int code() {
            return code;
        }

        @Nullable
        public Type getType() {
            return NncUtils.get(javaType, ModelDefRegistry::getType);
        }

        static ColumnType getByCode(int code) {
            return NncUtils.findRequired(values(), v -> v.code == code);
        }

    }

}
