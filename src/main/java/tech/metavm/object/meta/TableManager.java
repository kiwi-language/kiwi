package tech.metavm.object.meta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.object.meta.rest.dto.*;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

@Component
public class TableManager {

    @Autowired
    private TypeManager typeManager;

    @Autowired
    private EntityContextFactory entityContextFactory;

    public TableDTO get(long id) {
        EntityContext context = entityContextFactory.newContext();
        Type type = context.getType(id);
        return NncUtils.get(type, Type::toDTO, t -> convertToTable(t, context));
    }

    @Transactional
    public TableDTO save(TableDTO table) {
        EntityContext context = entityContextFactory.newContext();
        TypeDTO typeDTO = new TypeDTO(
                table.id(),
                table.name(),
                TypeCategory.CLASS.code(),
                table.ephemeral(),
                table.anonymous(),
                null,
                null,
                table.desc(),
                List.of(),
                List.of(),
                List.of()
        );
        Type type = typeManager.saveType(typeDTO, context);
        NncUtils.map(table.fields(), column -> saveField(column, context));
        saveTitleField(table.titleField(), type, context);
        context.finish();
        return convertToTable(type.toDTO(), context);
    }

    private void saveTitleField(TitleFieldDTO titleFieldDTO, Type type, EntityContext context) {
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
                        null,
                        titleFieldDTO.name(),
                        type,
                        Access.GLOBAL,
                        titleFieldDTO.unique(),
                        true,
                        null,
                        null,
                        titleFieldType,
                        context,
                        false
                );
            } else {
                titleField.setName(titleFieldDTO.name());
                titleField.setUnique(titleFieldDTO.unique());
                titleField.setDefaultValue(titleFieldDTO.defaultValue());
            }
        }
    }

    @Transactional
    public long saveColumn(ColumnDTO column) {
        EntityContext context = entityContextFactory.newContext();
        Field field = saveField(column, context);
        context.finish();
        return field.getId();
    }

    public ColumnDTO getColumn(long id) {
        EntityContext context = entityContextFactory.newContext();
        FieldDTO fieldDTO = context.getField(id).toDTO();
        if(fieldDTO == null) {
            return null;
        }
        return convertToColumnDTO(fieldDTO, context.getType(fieldDTO.typeId()));
    }

    private EnumEditContext saveEnum(ColumnDTO fieldEdit, EntityContext entityContext) {
        EnumEditContext enumEditContext = new EnumEditContext(
                fieldEdit.targetId(),
                fieldEdit.name(),
                true,
                fieldEdit.choiceOptions(),
                entityContext
        );
        enumEditContext.execute();
        return enumEditContext;
    }

    private Field saveField(ColumnDTO column, EntityContext context) {
        Type type;
        Object defaultValue;
        if(column.type() == ColumnType.ENUM.code) {
            EnumEditContext enumEditContext = saveEnum(column, context);
            type = getType(column, enumEditContext.getType(), context);
            defaultValue = column.multiValued() ?
                    NncUtils.map(enumEditContext.getDefaultOptions(), EnumConstant::getId)
                    : NncUtils.getFirst(enumEditContext.getDefaultOptions(), EnumConstant::getId);
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
                        column.ownerId(),
                        type.getId(),
                        null
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

    private TypeInfo getTypeInfo(Type type, Object fieldDefaultValue) {
        Type concreteType = type.getConcreteType();
        return new TypeInfo(
                getColumnType(type.getConcreteType()),
                concreteType.getName(),
                concreteType.isEnum() || concreteType.isClass() ? concreteType.getId() : null,
                type.isArray() ? type.getElementType().isNotNull() : type.isNotNull(),
                type.isArray(),
                getChoiceOptions(
                        NncUtils.sortByInt(concreteType.getEnumConstants(), EnumConstant::getOrdinal),
                        fieldDefaultValue
                )
        );
    }

    private ColumnType getColumnType(Type concreteType) {
        if(concreteType.isLong()) {
            return ColumnType.LONG;
        }
        if(concreteType.isDouble()) {
            return ColumnType.DOUBLE;
        }
        if(concreteType.isBool()) {
            return ColumnType.BOOL;
        }
        if(concreteType.isString()) {
            return ColumnType.STRING;
        }
        if(concreteType.isEnum()) {
            return ColumnType.ENUM;
        }
        if(concreteType.isClass()) {
            return ColumnType.TABLE;
        }
        if(concreteType.isTime()) {
            return ColumnType.TIME;
        }
        if(concreteType.isDate()) {
            return ColumnType.DATE;
        }
        throw new InternalException("Can not get column type for type: " + concreteType.getId());
    }

    private List<ChoiceOptionDTO> getChoiceOptions(List<EnumConstant> enumConstants, Object fieldDefaultValue) {
        return NncUtils.sortAndMap(
                enumConstants,
                Comparator.comparingInt(EnumConstant::getOrdinal),
                ec -> new ChoiceOptionDTO(
                        ec.getId(),
                        ec.getName(),
                        ec.getOrdinal(),
                        isPreselected(ec, fieldDefaultValue)
                )
        );
    }

    private boolean isPreselected(EnumConstant enumConstant, Object fieldDefaultValue) {
        if(fieldDefaultValue instanceof Long l) {
            return l.equals(enumConstant.getId());
        }
        if(fieldDefaultValue instanceof List list) {
            return list.contains(enumConstant.getId());
        }
        return false;
    }

    private Type getType(ColumnDTO column, Type concreteType, EntityContext context) {
        return getType(column.name(), column.type(), column.required(), column.multiValued(), concreteType, context);
    }

    private Type getType(String name, int columnTypeCode, boolean required, boolean multiValued,
                         Type concreteType, EntityContext context) {
        ColumnType columnType = ColumnType.getByCode(columnTypeCode);
        Type type;
        if(concreteType != null) {
            type = concreteType;
            if (!required) {
                type = type.getNullableType();
            }
            if (multiValued) {
                type = type.getArrayType();
            }
        }
        else if(columnType.getTypeId() != null) {
            type = context.getType(columnType.getTypeId());
        }
        else {
            throw BusinessException.invalidColumn(name, "未选择列类型或未选择关联表格");
        }
        if(type.getId() == null) {
            context.initIds();
        }
        return type;
    }

    public Page<TableDTO> list(String searchText, int page, int pageSize) {
        EntityContext context = entityContextFactory.newContext();
        Page<TypeDTO> typePage = typeManager.query(
                searchText,
                List.of(TypeCategory.CLASS.code()),
                page,
                pageSize,
                context
        );
        return new Page<>(
                NncUtils.map(typePage.data(), t -> convertToTable(t, context)),
                typePage.total()
        );
    }

    private TableDTO convertToTable(TypeDTO typeDTO, EntityContext context) {
        FieldDTO titleField = NncUtils.find(typeDTO.fields(), FieldDTO::asTitle);
        return new TableDTO(
                typeDTO.id(),
                typeDTO.name(),
                typeDTO.desc(),
                typeDTO.ephemeral(),
                typeDTO.anonymous(),
                NncUtils.get(titleField, f -> convertToTitleField(f, context)),
                NncUtils.map(typeDTO.fields(), f -> convertToColumnDTO(f , context.getType(f.typeId())))
        );
    }

    private TitleFieldDTO convertToTitleField(FieldDTO fieldDTO, EntityContext context) {
        return new TitleFieldDTO(
                fieldDTO.name(),
                getColumnType(context.getType(fieldDTO.typeId()).getConcreteType()).code,
                fieldDTO.unique(),
                fieldDTO.defaultValue()
        );
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

    private enum ColumnType {
        STRING(1, StdTypeConstants.STRING),
        DOUBLE(2, StdTypeConstants.DOUBLE),
        LONG(3, StdTypeConstants.LONG),
        BOOL(6, StdTypeConstants.BOOL),
        ENUM(7, null),
        TIME(9, StdTypeConstants.TIME),
        TABLE(10, null),
        DATE(13, StdTypeConstants.DATE),

        ;

        private final int code;

        @Nullable
        private final Long typeId;

        ColumnType(int code, @Nullable Long typeId) {
            this.code = code;
            this.typeId = typeId;
        }

        @Nullable
        public Long getTypeId() {
            return typeId;
        }

        static ColumnType getByCode(int code) {
            return NncUtils.findRequired(values(), v -> v.code == code);
        }

    }

}
