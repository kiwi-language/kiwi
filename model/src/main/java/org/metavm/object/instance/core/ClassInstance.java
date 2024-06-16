package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.common.ErrorCode;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.NoProxy;
import org.metavm.entity.ReadWriteArray;
import org.metavm.entity.natives.ListNative;
import org.metavm.flow.Flow;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClassInstance extends DurableInstance {

    public static final Logger logger = LoggerFactory.getLogger(ClassInstance.class);

    public static final Klass uninitializedKlass = KlassBuilder.newBuilder("Uninitialized", "Uninitialized").build();

    private final ReadWriteArray<InstanceField> fields = new ReadWriteArray<>(InstanceField.class);
    private final ReadWriteArray<UnknownField> unknownFields = new ReadWriteArray<>(UnknownField.class);
    private Klass klass;
    private transient Map<Flow, FlowInstance> functions;

    public static ClassInstance create(Map<Field, Instance> data, ClassType type) {
        return ClassInstanceBuilder.newBuilder(type).data(data).build();
    }

    public static ClassInstance allocate() {
        return allocate(uninitializedKlass.getType());
    }

    public static ClassInstance allocate(ClassType type) {
        return ClassInstanceBuilder.newBuilder(type).build();
    }

    public static ClassInstance allocate(ClassType type, @Nullable InstanceParentRef parentRef) {
        return ClassInstanceBuilder.newBuilder(type)
                .parentRef(parentRef)
                .build();
    }

    public ClassInstance(Id id, @NotNull ClassType type, long version, long syncVersion,
                         @Nullable Consumer<DurableInstance> load, @Nullable InstanceParentRef parentRef,
                         @Nullable Map<Field, Instance> data, @Nullable SourceRef sourceRef, boolean ephemeral) {
        super(id, type, version, syncVersion, ephemeral, load);
        this.klass = type.resolve();
        setParentRef(parentRef);
        setSourceRef(sourceRef);
        if (data != null)
            reset(data, 0L, 0L);
    }

    public ClassInstance(Id id, ClassType type, boolean ephemeral, @Nullable Consumer<DurableInstance> load) {
        super(id, type, 0, 0, ephemeral, load);
        this.klass = type.resolve();
    }

    public ClassInstance(Id id, Map<Field, Instance> data, Klass klass) {
        super(id, klass.getType(), 0, 0, klass.isEphemeral(), null);
        this.klass = klass;
        reset(data, 0L, 0L);
    }

    @NoProxy
    public void reset(Map<Field, Instance> data, long version, long syncVersion) {
//        try (var ignored = ContextUtil.getProfiler().enter("ClassInstance.reset")) {
        setModified();
        clear();
        setVersion(version);
        setSyncVersion(syncVersion);
        klass.forEachField(field -> {
//                try( var ignored1 = ContextUtil.getProfiler().enter("ClassInstance.reset.forEachField")) {
            Instance fieldValue = data.get(field);
            if (fieldValue == null || fieldValue.isNull()) {
                fieldValue = field.getDefaultValue();
            }
            initFieldInternal(field, fieldValue);
//                }
        });
        if (!isNew() && !isLoaded())
            setLoaded(false);
//        }
    }


    private void clear() {
//        try (var ignored = ContextUtil.getProfiler().enter("ClassInstance.clear")) {
        new ArrayList<>(getOutgoingReferences()).forEach(ReferenceRT::clear);
        this.fields.clear();
        this.unknownFields.clear();
//        }
    }

    public void forEachField(BiConsumer<Field, Instance> action) {
        ensureLoaded();
        for (InstanceField field : fields) {
            action.accept(field.getField(), field.getValue());
        }
    }

    public void forEachUnknownField(Consumer<Instance> action) {
        ensureLoaded();
        unknownFields.forEach(f -> action.accept(f.getValue()));
    }

    public Set<IndexKeyRT> getIndexKeys() {
        ensureLoaded();
        return NncUtils.flatMapUnique(
                klass.getConstraints(Index.class),
                c -> c.createIndexKey(this)
        );
    }

    public Set<DurableInstance> getRefInstances() {
        ensureLoaded();
        Set<DurableInstance> result = new IdentitySet<>();
        for (InstanceField field : fields) {
            Instance fieldValue = field.getValue();
            if (fieldValue instanceof DurableInstance d) {
                result.add(d);
            }
        }
        for (UnknownField unknownField : unknownFields) {
            Instance fieldValue = unknownField.getValue();
            if (fieldValue instanceof DurableInstance d)
                result.add(d);
        }
        return result;
    }

    private void addField(InstanceField field) {
        NncUtils.requireTrue(fields.get(InstanceField::getField, field) == null,
                () -> new InternalException(
                        String.format("Field '%s' is already added to instance '%s'", field.getName(), this)
                )
        );
        this.fields.add(field);
    }

    public String getTitle() {
        ensureLoaded();
        Field titleField = klass.getTitleField();
        return titleField != null ? field(titleField).getDisplayValue() : tryGetTreeId() + "";
    }

    public Object getField(List<Id> fieldPath) {
        ensureLoaded();
        var fieldId = fieldPath.get(0);
        InstanceField field = field(fieldId);
        if (fieldPath.size() > 1) {
            var subFieldPath = fieldPath.subList(1, fieldPath.size());
            return NncUtils.get((ClassInstance) field.getValue(), inst -> inst.getField(subFieldPath));
        } else {
            return field.getValue();
        }
    }

    public ClassType getType() {
        ensureLoaded();
        return (ClassType) super.getType();
    }

    @NoProxy
    public boolean isReference() {
        return !isValue();
    }

    public Instance getInstanceField(Field field) {
        ensureLoaded();
        return field(field).getValue();
    }

    public void setUnknownField(Klass declaringType, Column column, Instance value) {
        ensureLoaded();
        var field = NncUtils.find(unknownFields,
                f -> f.getDeclaringType() == declaringType && f.getColumn() == column);
        if (field != null) {
            field.setValue(value);
        } else {
            unknownFields.add(new UnknownField(declaringType, column, value));
        }
    }

    @Override
    public boolean isChild(DurableInstance instance) {
        return instance.getParent() == this;
    }

    public Set<DurableInstance> getChildren() {
        ensureLoaded();
        var children = new IdentitySet<DurableInstance>();
        for (InstanceField field : fields) {
            if (field.getField().isChild()) {
                Instance fieldValue = field.getValue();
                if (fieldValue.isNotNull()) {
                    children.add((DurableInstance) fieldValue);
                }
            }
        }
        return children;
    }

    @Override
    protected void writeBody(InstanceOutput output) {
        ensureLoaded();
        var fields = this.fields;
        fields.sort(Comparator.comparingLong(InstanceField::getRecordGroupTag).thenComparing(InstanceField::getRecordTag));
        var sortedInstanceFields = new ArrayList<List<InstanceField>>();
        var currentFields = new ArrayList<InstanceField>();
        for (InstanceField field : fields) {
            if (field.shouldSkipWrite())
                continue;
            if (currentFields.isEmpty() || currentFields.get(0).getField().getDeclaringType() == field.getField().getDeclaringType())
                currentFields.add(field);
            else {
                sortedInstanceFields.add(currentFields);
                currentFields = new ArrayList<>();
                currentFields.add(field);
            }
        }
        if (!currentFields.isEmpty())
            sortedInstanceFields.add(currentFields);
        output.writeInt(sortedInstanceFields.size());
        for (List<InstanceField> instanceFields : sortedInstanceFields) {
            output.writeLong(instanceFields.get(0).getField().getRecordGroupTag());
            output.writeInt(instanceFields.size());
            for (InstanceField field : instanceFields) {
                output.writeLong(field.getField().getRecordTag());
                var fieldValue = field.getValue();
                if (fieldValue.isValue() || field.getField().isChild() && !field.getField().isLazy())
                    output.writeRecord(fieldValue);
                else
                    output.writeInstance(fieldValue);
            }
        }
    }

    @Override
    @NoProxy
    public void setType(Type type) {
        if (type instanceof ClassType classType) {
            klass = classType.resolve();
            super.setType(type);
        } else
            throw new IllegalArgumentException(type + " is not a class type");
    }

    @Override
    @NoProxy
    public void readFrom(InstanceInput input) {
        setLoaded(input.isLoadedFromCache());
        List<List<Field>> sortedFields = klass.getSortedKlassAndFields();
        var instFields = this.fields;
        int j = 0;
        int numKlasses = input.readInt();
        for (int i = 0; i < numKlasses; i++) {
            var groupTag = input.readLong();
            int cmp = 1;
            while (j < sortedFields.size() && (cmp = Long.compare(sortedFields.get(j).get(0).getRecordGroupTag(), groupTag)) < 0) {
                for (var field : sortedFields.get(j))
                    instFields.add(new InstanceField(this, field, Instances.nullInstance(), false));
                j++;
            }
            if (cmp == 0) {
                var fields = sortedFields.get(j++);
                int m = 0;
                int numFields = input.readInt();
                for (int l = 0; l < numFields; l++) {
                    var fieldTag = input.readLong();
                    while (m < fields.size() && fields.get(m).getRecordTag() < fieldTag) {
                        instFields.add(new InstanceField(this, fields.get(m), Instances.nullInstance(), false));
                        m++;
                    }
                    Field field;
                    if (m < fields.size() && (field = fields.get(m)).getRecordTag() == fieldTag) {
                        input.setParent(this, field);
                        var value = input.readInstance();
                        instFields.add(new InstanceField(this, field, value, false));
                        m++;
                    } else // the field corresponding to the fieldId has been removed
                        input.skipInstance();
                }
                input.setParent(getParent(), getParentField());
                for (; m < fields.size(); m++)
                    instFields.add(new InstanceField(this, fields.get(m), Instances.nullInstance(), false));
            } else {
                int numFields = input.readInt();
                for (int k = 0; k < numFields; k++) {
                    input.readLong();
                    input.skipInstance();
                }
            }
        }
        for (; j < sortedFields.size(); j++) {
            for (Field field : sortedFields.get(j))
                instFields.add(new InstanceField(this, field, Instances.nullInstance(), false));
        }
    }

    public ClassInstance getClassInstance(Field field) {
        ensureLoaded();
        return (ClassInstance) field(field).getValue();
    }

    public Instance getField(String fieldPath) {
        ensureLoaded();
        int idx = fieldPath.indexOf('.');
        if (idx == -1) {
            return getField(klass.getFieldNyNameRequired(fieldPath));
        } else {
            String fieldName = fieldPath.substring(0, idx);
            String subPath = fieldPath.substring(idx + 1);
            ClassInstance fieldInstance = (ClassInstance) getInstanceField(fieldName);
            return NncUtils.get(fieldInstance, inst -> inst.getField(subPath));
        }
    }

    public Instance getInstanceField(String fieldName) {
        ensureLoaded();
        return field(klass.tryGetFieldByName(fieldName)).getValue();
    }

    public void setField(String fieldCode, Instance value) {
        var field = klass.getFieldByCode(fieldCode);
        setFieldInternal(field, value);
    }

    public void setField(Field field, Instance value) {
        ensureLoaded();
        setFieldInternal(field, value);
    }

    void setOrInitField(Field field, Instance value) {
        ensureLoaded();
        if (isFieldInitialized(field))
            setFieldInternal(field, value);
        else
            initFieldInternal(field, value);
    }

    @Override
    public ClassInstance tryGetSource() {
        return (ClassInstance) super.tryGetSource();
    }

    private void setFieldInternal(Field field, Instance value) {
        ensureLoaded();
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(klass));
        if (field.isReadonly())
            throw new BusinessException(ErrorCode.CAN_NOT_MODIFY_READONLY_FIELD);
        if (field.isChild() && value.isNotNull())
            ((DurableInstance) value).setParent(this, field);
        setModified();
        field(field).setValue(value);
    }

    public boolean isFieldInitialized(Field field) {
        ensureLoaded();
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(klass));
        return fields.get(InstanceField::getField, field) != null;
    }

    public boolean isAllFieldsInitialized() {
        ensureLoaded();
        return klass.allFieldsMatch(this::isFieldInitialized);
    }

    public @Nullable Field findUninitializedField(Klass type) {
        ensureLoaded();
        NncUtils.requireTrue(type.isAssignableFrom(klass));
        return type.findField(f -> !isFieldInitialized(f));
    }

    public void initField(Field field, Instance value) {
        ensureLoaded();
        initFieldInternal(field, value);
    }

    private void initFieldInternal(Field field, Instance value) {
//        try (var ignored = ContextUtil.getProfiler().enter("ClassInstance.initFieldInternal")) {
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(klass));
        NncUtils.requireFalse(isFieldInitialized(field));
        if (field.isChild() && value.isNotNull())
            ((DurableInstance) value).setParent(this, field);
        addField(new InstanceField(this, field, value));
//        }
    }

    public StringInstance getStringField(Field field) {
        return (StringInstance) getField(field(field).getField());
    }

    public LongInstance getLongField(Field field) {
        return (LongInstance) getField(field);
    }

    public DoubleInstance getDoubleField(Field field) {
        return (DoubleInstance) getField(field);
    }

    public Instance getField(Field field) {
        ensureLoaded();
        return field(field).getValue();
    }

    public FlowInstance getFunction(Method method) {
        ensureLoaded();
        if (functions == null)
            functions = new HashMap<>();
        var concreteFlow = klass.tryResolveMethod(method);
        return functions.computeIfAbsent(concreteFlow,
                k -> new FlowInstance(klass.tryResolveMethod(method), this));
    }

    public Instance getProperty(Property property) {
        return switch (property) {
            case Field field -> getField(field);
            case Method method -> getFunction(method);
            default -> throw new IllegalStateException("Unexpected value: " + property);
        };
    }

    protected InstanceField field(Field field) {
        var instanceField = fields.get(InstanceField::getField, field);
        if (instanceField != null)
            return instanceField;
        var unknownField = unknownFields.get(UnknownField::getColumn, field.getColumn());
        if (unknownField != null) {
            unknownFields.remove(unknownField);
            instanceField = new InstanceField(this, field, unknownField.getValue());
            addField(instanceField);
            return instanceField;
        }
        if (DebugEnv.resolveVerbose) {
            logger.info("initialized: {}, persisted: {}", isInitialized(), isPersisted());
            logger.info("fields");
            klass.forEachField(f -> logger.info("field: {}", f.getQualifiedName()));
            logger.info("Instance fields:");
            fields.forEach(f -> logger.info("field: {}", f.getField().getQualifiedName()));
        }
        throw new InternalException("Can not find instance field for '" + field + "'");
    }

    protected InstanceField field(Id fieldId) {
        return field(klass.getField(fieldId));
    }

    @Override
    protected InstanceParam getParam() {
        ensureLoaded();
        if (isList()) {
            var elements = new ListNative(this).toArray().getElements();
            if (isChildList()) {
                return new ListInstanceParam(
                        true,
                        NncUtils.map(elements, e ->
                                new InstanceFieldValue(
                                        e.getTitle(), e.toDTO()
                                )
                        )
                );
            } else {
                return new ListInstanceParam(
                        false,
                        NncUtils.map(elements, Instance::toFieldValueDTO)
                );
            }
        } else
            return new ClassInstanceParam(NncUtils.map(fields, InstanceField::toDTO));
    }

    @Override
    @NoProxy
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitClassInstance(this);
    }

    @Override
    public <R> void acceptReferences(InstanceVisitor<R> visitor) {
        ensureLoaded();
        for (var field : fields)
            field.getValue().accept(visitor);
        for (var unknownField : unknownFields)
            unknownField.getValue().accept(visitor);
    }

    @Override
    public <R> void acceptChildren(InstanceVisitor<R> visitor) {
        ensureLoaded();
        for (var field : fields) {
            if (field.getField().isChild())
                field.getValue().accept(visitor);
        }
    }

    @Override
    protected void writeTree(TreeWriter treeWriter) {
        ensureLoaded();
        treeWriter.writeLine(getType().getName() + " " + getTitle());
        treeWriter.indent();
        for (InstanceField field : fields) {
            treeWriter.writeLine(field.getName() + ":");
            treeWriter.indent();
            if (field.getField().isChild())
                field.getValue().writeTree(treeWriter);
            else
                treeWriter.writeLine(field.getValue().getTitle());
            treeWriter.deIndent();
        }
        treeWriter.deIndent();
    }

    @Override
    public FieldValue toFieldValueDTO() {
        ensureLoaded();
        if (isValue() || isList()) {
            return new InstanceFieldValue(
                    getTitle(),
                    toDTO()
            );
        } else {
            return new ReferenceFieldValue(
                    getTitle(),
                    Objects.requireNonNull(this.getStringIdForDTO(), "Id required"),
                    getType().toExpression());
        }
    }

    public boolean isList() {
        return klass.isList();
    }

    public boolean isChildList() {
        return klass.isChildList();
    }

    public List<InstanceField> fields() {
        ensureLoaded();
        return fields.toList();
    }

    public int debugFieldsSize() {
        return this.fields.size();
    }

    public ArrayInstance getInstanceArray(Field field) {
        ensureLoaded();
        return field(field).getInstanceArray();
    }

    public ArrayInstance getInstanceArray(String fieldName) {
        ensureLoaded();
        return field(klass.tryGetFieldByName(fieldName)).getInstanceArray();
    }

    @Override
    public Object toJson(IEntityContext context) {
        if(isList()) {
            var listNative = new ListNative(this);
            var array = listNative.toArray();
            var list = new ArrayList<>();
            array.forEach(e -> list.add(e.toJson( context)));
            return list;
        }
        else {
            var map = new HashMap<String, Object>();
            forEachField((f, v) -> {
                if(f.isPublic())
                    map.put(f.getCode(), v.toJson(context));
            });
            getKlass().forEachMethod(m -> {
                if(m.isGetter())
                    map.put(m.getPropertyName(), Flows.invokeGetter(m, this, context));
            });
            return map;
        }
    }

    private void ensureFieldInitialized(Field field) {
        if (fields.get(InstanceField::getField, field) != null) {
            return;
        }
        var unknownField = unknownFields.remove(UnknownField::getColumn, field.getColumn());
        if (unknownField != null) {
            initFieldInternal(field, unknownField.getValue());
            return;
        }
        throw new BusinessException(
                ErrorCode.FIELD_NOT_INITIALIZED,
                getType().getName(),
                field.getName()
        );
    }

    public void ensureAllFieldsInitialized() {
        ensureLoaded();
        for (Field field : klass.getAllFields()) {
            ensureFieldInitialized(field);
        }
    }

    public Klass getKlass() {
        ensureLoaded();
        return klass;
    }

    @Override
    public boolean isMutable() {
        return getKlass().getKind() != ClassKind.VALUE;
    }
}
