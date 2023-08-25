package tech.metavm.object.meta;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.*;
import tech.metavm.object.instance.rest.ArrayFieldValueDTO;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.instance.rest.ReferenceFieldValueDTO;
import tech.metavm.object.meta.rest.dto.*;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static tech.metavm.object.meta.TypeUtil.*;
import static tech.metavm.util.NncUtils.requireNonNull;

@Component
public class TableManager {

    private final TypeManager typeManager;

    private final InstanceContextFactory instanceContextFactory;

    public TableManager(TypeManager typeManager, InstanceContextFactory instanceContextFactory) {
        this.typeManager = typeManager;
        this.instanceContextFactory = instanceContextFactory;
    }

    public TableDTO get(long id) {
        IEntityContext context = newContext();
        ClassType type = context.getClassType(id);
        return NncUtils.get(type, t -> t.toDTO(true, false), t -> convertToTable(t, context));
    }

    @Transactional
    public TableDTO save(TableDTO table) {
        IEntityContext context = newContext();
        TypeDTO typeDTO = new TypeDTO(
                table.id(),
                null,
                table.name(),
                table.code(),
                TypeCategory.CLASS.code(),
                table.ephemeral(),
                table.anonymous(),
                null,
                null,
                new ClassParamDTO(
                        null,
                        null,
                        List.of(),
                        List.of(),
                        List.of(),
                        null,
                        null
                )
        );
        ClassType type = typeManager.saveType(typeDTO, context);
        context.initIds();
        NncUtils.map(table.fields(), column -> saveField(column, type, context));
        saveTitleField(table.titleField(), type, context);
        context.finish();
        return convertToTable(type.toDTO(), context);
    }

    private void saveTitleField(TitleFieldDTO titleFieldDTO, ClassType type, IEntityContext context) {
        if(titleFieldDTO != null) {
            Field titleField = type.getTileField();
            if (titleField == null) {
                Type titleFieldType = getType(
                        titleFieldDTO.name(),
                        titleFieldDTO.type(),
                        true,
                        false,
                        null,
                        context
                );
                new Field(
                        titleFieldDTO.name(),
                        type,
                        Access.GLOBAL,
                        titleFieldDTO.unique(),
                        true,
                        InstanceUtils.nullInstance(),
                        titleFieldType,
                        false
                );
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
        return field.getId();
    }

    public ColumnDTO getColumn(long id) {
        InstanceContext context = instanceContextFactory.newContext();
        FieldDTO fieldDTO = context.getEntityContext().getField(id).toDTO();
        if(fieldDTO == null || !isVisible(fieldDTO, context.getEntityContext())) {
            return null;
        }
        return convertToColumnDTO(fieldDTO, context.getType(fieldDTO.typeId()));
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
        FieldValueDTO defaultValue;
        if(column.type() == ColumnType.ENUM.code) {
            EnumEditContext enumEditContext = saveEnum(declaringType, column, context);
            type = getType(column, enumEditContext.getType(), context);
            defaultValue = column.multiValued() ?
                    new ArrayFieldValueDTO(
                            null,
                            NncUtils.map(enumEditContext.getDefaultOptions(), EnumConstantRT::toFieldValue)
                    )
                    : NncUtils.getFirst(enumEditContext.getDefaultOptions(), EnumConstantRT::toFieldValue);
        }
        else {
            type = getType(column, NncUtils.get(column.targetId(), context::getType), context);
            defaultValue = column.defaultValue();
        }
        return typeManager.saveField(
                new FieldDTO(
                        column.id(),
                        column.name(),
                        column.access(),
                        defaultValue,
                        column.unique(),
                        column.asTitle(),
                        declaringType.getId(),
                        type.getId(),
                        null,
                        false
                ),
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
                field.asTitle(),
                field.defaultValue(),
                typeInfo.choiceOptions
        );
    }

    private TypeInfo getTypeInfo(Type type, FieldValueDTO fieldDefaultValue) {
        Type concreteType = type.getConcreteType();
        return new TypeInfo(
                getColumnType(concreteType),
                concreteType.getName(),
                concreteType.isEnum() || concreteType.isClass() || concreteType.isValue() ?
                        concreteType.getId() : null,
                type.isNotNull(),
                type.isUnionNullable() ? type.getUnderlyingType().isArray() : type.isArray(),
                getChoiceOptions(concreteType, fieldDefaultValue)
        );
    }

    private List<ChoiceOptionDTO> getChoiceOptions(Type type, FieldValueDTO fieldDefaultValue) {
        if(type instanceof EnumType enumType) {
            return getChoiceOptions(
                    NncUtils.sortByInt(enumType.getEnumConstants(), EnumConstantRT::getOrdinal),
                    fieldDefaultValue
            );
        }
        else {
            return List.of();
        }
    }

    private ColumnType getColumnType(Type type) {
        if(isLong(type)) {
            return ColumnType.LONG;
        }
        if(isInt(type)) {
            return ColumnType.INT;
        }
        if(isDouble(type)) {
            return ColumnType.DOUBLE;
        }
        if(isBool(type)) {
            return ColumnType.BOOL;
        }
        if(isString(type)) {
            return ColumnType.STRING;
        }
        if(type.isEnum()) {
            return ColumnType.ENUM;
        }
        if(type.isClass() || type.isValue()) {
            return ColumnType.TABLE;
        }
        if(isTime(type)) {
            return ColumnType.TIME;
        }
        if(type instanceof AnyType) {
            return ColumnType.ANY;
        }
        throw new InternalException("Can not get column type for type: " + type);
    }

    private List<ChoiceOptionDTO> getChoiceOptions(List<EnumConstantRT> enumConstants, FieldValueDTO fieldDefaultValue) {
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

    private boolean isPreselected(EnumConstantRT enumConstant, FieldValueDTO fieldDefaultValue) {
        if(fieldDefaultValue instanceof ReferenceFieldValueDTO ref) {
            return ref.getId() == enumConstant.getId();
        }
        if(fieldDefaultValue instanceof ArrayFieldValueDTO array) {
            for (FieldValueDTO element : array.getElements()) {
                if(element instanceof ReferenceFieldValueDTO ref) {
                    if(ref.getId() == enumConstant.getId()) {
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
        if(concreteType == null && columnType.getType() != null) {
            concreteType = columnType.getType();
        }
        Type type;
        if(concreteType != null) {
            type = concreteType;
            if (multiValued) {
                type = getArrayType(type);
            }
            if (!required) {
                type = getNullableType(type);
            }
        }
        else {
            throw BusinessException.invalidColumn(name, "未选择列类型或未选择关联表格");
        }
        if(type.getId() == null) {
            if(!context.containsModel(type)){
                context.bind(type);
            }
            context.initIds();
        }
        return type;
    }

    public Page<TableDTO> list(String searchText, int page, int pageSize) {
        IEntityContext context = newContext();
        Page<TypeDTO> typePage = typeManager.query(
                searchText,
                List.of(TypeCategory.CLASS.code(), TypeCategory.VALUE.code()),
                page,
                pageSize,
                context
        );
        return new Page<>(
                NncUtils.map(typePage.data(), t -> convertToTable(t, context)),
                typePage.total()
        );
    }

    private TableDTO convertToTable(TypeDTO typeDTO, IEntityContext context) {
        ClassParamDTO param = (ClassParamDTO) typeDTO.param();
        FieldDTO titleField = NncUtils.find(param.fields(), FieldDTO::asTitle);
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
                        f -> convertToColumnDTO(f , context.getType(f.typeId()))
                )
        );
    }

    private static final Set<Class<?>> CONFIDENTIAL_JAVA_CLASSES = Set.of(Password.class);

    private boolean isVisible(FieldDTO fieldDTO, IEntityContext context) {
        NncUtils.requireNonNull(fieldDTO.typeId(), "字段'" + fieldDTO.name() + "'的typeId为空");
        Type fieldType = context.getType(fieldDTO.typeId());
        if(ModelDefRegistry.containsTypeDef(fieldType)) {
            Class<?> javaClass = ModelDefRegistry.getJavaClass(fieldType);
            return !CONFIDENTIAL_JAVA_CLASSES.contains(javaClass);
        }
        else {
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
    
    private IEntityContext newContext() {
        return instanceContextFactory.newContext().getEntityContext();
    }

    private record TypeInfo (
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
