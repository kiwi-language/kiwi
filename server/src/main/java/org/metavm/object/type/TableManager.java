package org.metavm.object.type;

import org.metavm.common.Page;
import org.metavm.entity.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.rest.ArrayFieldValue;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.ReferenceFieldValue;
import org.metavm.object.type.rest.dto.*;
import org.metavm.util.BusinessException;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.metavm.util.Password;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.*;

import static org.metavm.object.type.Types.*;
import static org.metavm.util.NncUtils.requireNonNull;

@Component
public class TableManager extends EntityContextFactoryAware {

    private final TypeManager typeManager;


    public TableManager(EntityContextFactory entityContextFactory, TypeManager typeManager) {
        super(entityContextFactory);
        this.typeManager = typeManager;
    }

    public TableDTO get(String id) {
        try (IEntityContext context = newContext()) {
            Klass type = context.getKlass(id);
            try(var serContext = SerializeContext.enter()) {
                return NncUtils.get(type, t -> t.toDTO(serContext), t -> convertToTable(t, context));
            }
        }
    }

    @Transactional
    public TableDTO save(TableDTO table) {
        IEntityContext context = newContext();
        KlassDTO klassDTO = ClassTypeDTOBuilder.newBuilder(table.name())
                .id(table.id())
                .code(table.code())
                .ephemeral(table.ephemeral())
                .anonymous(table.anonymous())
                .build();
        var klass = typeManager.saveType(klassDTO, context);
        context.initIds();
        NncUtils.map(table.fields(), column -> saveField(column, klass, context));
        saveTitleField(table.titleField(), klass, context);
        context.finish();
        try(var serContext = SerializeContext.enter()) {
            return convertToTable(klass.toDTO(serContext), context);
        }
    }

    private void saveTitleField(TitleFieldDTO titleFieldDTO, Klass type, IEntityContext context) {
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
                        .tmpId(titleFieldDTO.tmpId())
                        .build();
                type.setTitleField(titleField);
            } else {
                titleField.setName(titleFieldDTO.name());
                titleField.setUnique(titleFieldDTO.unique());
            }
        }
    }

    @Transactional
    public String saveColumn(ColumnDTO column) {
        requireNonNull(column.ownerId(), () -> BusinessException.invalidParams("table Id require"));
        IEntityContext context = newContext();
        var declaringType = context.getKlass(Id.parse(column.ownerId()));
        Field field = saveField(column, declaringType, context);
        context.finish();
        return field.getStringId();
    }

    public ColumnDTO getColumn(String id) {
        try (var context = newContext()) {
            FieldDTO fieldDTO = context.getField(id).toDTO();
            if (fieldDTO == null || !isVisible(fieldDTO, context)) {
                return null;
            }
            return convertToColumnDTO(fieldDTO, org.metavm.object.type.TypeParser.parseType(fieldDTO.type(), context));
        }
    }

    private EnumEditContext saveEnum(Klass declaringKlass, ColumnDTO fieldEdit, IEntityContext context) {
        EnumEditContext enumEditContext = new EnumEditContext(
                fieldEdit.targetId(),
                declaringKlass.getName() + "_" + fieldEdit.name(),
                true,
                fieldEdit.choiceOptions(),
                context
        );
        enumEditContext.execute();
        return enumEditContext;
    }

    private Field saveField(ColumnDTO column, Klass declaringKlass, IEntityContext context) {
        Type type;
        FieldValue defaultValue;
        if (column.type() == ColumnType.ENUM.code) {
            EnumEditContext enumEditContext = saveEnum(declaringKlass, column, context);
            type = getType(column, enumEditContext.getType().getType(), context);
            defaultValue = column.multiValued() ?
                    new ArrayFieldValue(
                            null,
                            false,
                            NncUtils.map(enumEditContext.getDefaultOptions(), enumConstantRT -> enumConstantRT.toFieldValue(context.getInstanceContext()))
                    )
                    : NncUtils.first(enumEditContext.getDefaultOptions(), enumConstantRT1 -> enumConstantRT1.toFieldValue(context.getInstanceContext()));
        } else {
            type = getType(column, NncUtils.get(column.targetId(), id -> org.metavm.object.type.TypeParser.parseType(id, context)), context);
            defaultValue = column.defaultValue();
        }
        return typeManager.saveField(
                FieldDTOBuilder.newBuilder(column.name(), type.toExpression())
                        .id(column.id())
                        .access(column.access())
                        .defaultValue(defaultValue)
                        .unique(column.unique())
                        .declaringTypeId(declaringKlass.getStringId())
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
                        concreteType.getStringId() : null,
                type.isNotNull(),
                type.isBinaryNullable() ? type.getUnderlyingType().isArray() : type.isArray(),
                getChoiceOptions(concreteType, fieldDefaultValue)
        );
    }

    private List<ChoiceOptionDTO> getChoiceOptions(Type type, FieldValue fieldDefaultValue) {
        if (type instanceof ClassType classType && type.isEnum()) {
            var klass = classType.resolve();
            var enumConstants = NncUtils.map(
                    klass.getEnumConstants(),
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
        if (isBoolean(type)) {
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
        if (type instanceof AnyType || type instanceof VariableType) {
            return ColumnType.ANY;
        }
        throw new InternalException("Can not get column type for type: " + type);
    }

    private List<ChoiceOptionDTO> getChoiceOptions(List<EnumConstantRT> enumConstants, FieldValue fieldDefaultValue) {
        return NncUtils.sortAndMap(
                enumConstants,
                Comparator.comparingInt(EnumConstantRT::getOrdinal),
                ec -> new ChoiceOptionDTO(
                        ec.getInstanceIdString(),
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
                type = new ArrayType(type, ArrayKind.READ_WRITE);
            }
            if (!required) {
                type = getNullableType(type);
            }
        } else {
            throw BusinessException.invalidColumn(name, "column type required");
        }
        if (type.tryGetId() == null) {
            if (!context.containsEntity(type)) {
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
            Page<KlassDTO> typePage = typeManager.query(request);
            return new Page<>(
                    NncUtils.map(typePage.data(), t -> convertToTable(t, context)),
                    typePage.total()
            );
        }
    }

    private TableDTO convertToTable(KlassDTO klassDTO, IEntityContext context) {
        FieldDTO titleField = klassDTO.titleFieldId() != null ?
                NncUtils.find(klassDTO.fields(), f -> f.id().equals(klassDTO.titleFieldId())) : null;
        return new TableDTO(
                klassDTO.id(),
                klassDTO.name(),
                klassDTO.code(),
                klassDTO.desc(),
                klassDTO.ephemeral(),
                klassDTO.anonymous(),
                NncUtils.get(titleField, f -> convertToTitleField(f, context)),
                NncUtils.filterAndMap(
                        klassDTO.fields(),
                        f -> isVisible(f, context),
                        f -> convertToColumnDTO(f, org.metavm.object.type.TypeParser.parseType(f.type(), context))
                )
        );
    }

    private static final Set<Class<?>> CONFIDENTIAL_JAVA_CLASSES = Set.of(Password.class);

    private boolean isVisible(FieldDTO fieldDTO, IEntityContext context) {
        NncUtils.requireNonNull(fieldDTO.type(), "type is missing for field '" + fieldDTO.name() + "'");
        Type fieldType = org.metavm.object.type.TypeParser.parseType(fieldDTO.type(), context);
        if (fieldType instanceof ClassType classType) {
            var klass = classType.resolve();
            if(ModelDefRegistry.containsDef(klass)) {
                Class<?> javaClass = ModelDefRegistry.getJavaClass(fieldType);
                return !CONFIDENTIAL_JAVA_CLASSES.contains(javaClass);
            }
        }
        return true;
    }

    private TitleFieldDTO convertToTitleField(FieldDTO fieldDTO, IEntityContext context) {
        Type fieldType = TypeParser.parseType(fieldDTO.type(), context);
        return new TitleFieldDTO(
                null,
                fieldDTO.name(),
                getColumnType(fieldType.getConcreteType()).code,
                fieldDTO.unique(),
                fieldDTO.defaultValue()
        );
    }

    private record TypeInfo(
            ColumnType columnType,
            String name,
            String concreteTypeId,
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
