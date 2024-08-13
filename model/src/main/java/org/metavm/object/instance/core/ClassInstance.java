package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.NoProxy;
import org.metavm.entity.natives.ListNative;
import org.metavm.flow.Flow;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClassInstance extends Instance {

    public static final Logger logger = LoggerFactory.getLogger(ClassInstance.class);

    public static final Klass uninitializedKlass = KlassBuilder.newBuilder("Uninitialized", "Uninitialized").build();

    private final FieldTable fieldTable = new FieldTable(this);
    private Klass klass;
    private transient Map<Flow, FlowValue> functions;

    public static ClassInstance create(Map<Field, Value> data, ClassType type) {
        return ClassInstanceBuilder.newBuilder(type).data(data).build();
    }

    public static ClassInstance allocate(ClassType type) {
        return ClassInstanceBuilder.newBuilder(type).build();
    }

    public static ClassInstance allocateUninitialized(Id id) {
        return ClassInstanceBuilder.newBuilder(uninitializedKlass.getType()).id(id).initFieldTable(false).build();
    }

    public static ClassInstance allocateEmpty(ClassType type) {
        return ClassInstanceBuilder.newBuilder(type).initFieldTable(false).build();
    }

    public static ClassInstance allocate(ClassType type, @Nullable InstanceParentRef parentRef) {
        return ClassInstanceBuilder.newBuilder(type)
                .parentRef(parentRef)
                .build();
    }

    public ClassInstance(Id id, @NotNull ClassType type, long version, long syncVersion,
                         @Nullable Consumer<Instance> load, @Nullable InstanceParentRef parentRef,
                         @Nullable Map<Field, Value> data, @Nullable SourceRef sourceRef, boolean ephemeral, boolean initFieldTable) {
        super(id, type, version, syncVersion, ephemeral, load);
        this.klass = type.resolve();
        if (klass != uninitializedKlass && initFieldTable)
            fieldTable.initialize();
        setParentRef(parentRef);
        setSourceRef(sourceRef);
        if (data != null)
            reset(data, 0L, 0L);
    }

    public ClassInstance(Id id, ClassType type, boolean ephemeral, @Nullable Consumer<Instance> load) {
        super(id, type, 0, 0, ephemeral, load);
        this.klass = type.resolve();
        if (klass != uninitializedKlass)
            fieldTable.initialize();
    }

    public ClassInstance(Id id, Map<Field, Value> data, Klass klass) {
        super(id, klass.getType(), 0, 0, klass.isEphemeral(), null);
        this.klass = klass;
        if (klass != uninitializedKlass)
            fieldTable.initialize();
        reset(data, 0L, 0L);
    }

    @NoProxy
    public void reset(Map<Field, Value> data, long version, long syncVersion) {
//        try (var ignored = ContextUtil.getProfiler().enter("ClassInstance.reset")) {
        setModified();
        clear();
        setVersion(version);
        setSyncVersion(syncVersion);
        klass.forEachField(field -> {
//                try( var ignored1 = ContextUtil.getProfiler().enter("ClassInstance.reset.forEachField")) {
            Value fieldValue = data.get(field);
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
        fieldTable.clear();
    }

    public void logFields() {
        fieldTable.forEachField((field, value) -> logger.info("Field: {}, Value: {}", field.getName(), value));
    }

    public void logFieldTable() {
        for (IInstanceField field : fieldTable) {
            logger.debug("Klass tag {}, field tag {}, value {}, unknown: {}",
                    field.getKlassTag(), field.getTag(), field.getValue(), field instanceof UnknownField);
        }
    }

    public void forEachField(BiConsumer<Field, Value> action) {
        ensureLoaded();
        fieldTable.forEachField(action);
    }

    public Set<IndexKeyRT> getIndexKeys() {
        ensureLoaded();
        return NncUtils.flatMapUnique(
                klass.getConstraints(Index.class),
                c -> c.createIndexKey(this)
        );
    }

    public Set<Instance> getRefInstances() {
        ensureLoaded();
        Set<Instance> result = new IdentitySet<>();
        forEachField((f, v) -> {
            if (v instanceof Reference r)
                result.add(r.resolve());
        });
        return result;
    }

    private void addField(Field field, Value value) {
        fieldTable.get(field).set(value);
    }

    public String getTitle() {
        ensureLoaded();
        Field titleField = klass.getTitleField();
        return titleField != null ? field(titleField).getDisplayValue() : getStringId();
    }

    @Override
    public void forEachChild(Consumer<Instance> action) {
        forEachField((f, v) -> {
            if(f.isChild() && v instanceof Reference r)
                action.accept(r.resolve());
        });
    }

    @Override
    public void forEachMember(Consumer<Instance> action) {
        forEachField((f, v) -> {
            if(v instanceof Reference r && (f.isChild() || r.isValueReference()))
                action.accept(r.resolve());
        });
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        forEachField((f, v) -> {
            if(v instanceof Reference r)
                action.accept(r);
        });
    }

    @Override
    public void forEachReference(BiConsumer<Reference, Boolean> action) {
        forEachField((f, v) -> {
            if(v instanceof Reference r)
                action.accept(r, f.isChild());
        });
    }

    @Override
    public void forEachReference(TriConsumer<Reference, Boolean, Type> action) {
        forEachField((f, v) -> {
            if(v instanceof Reference r)
                action.accept(r, f.isChild(), f.getType());
        });
    }

    @Override
    public void transformReference(TriFunction<Reference, Boolean, Type, Reference> function) {
        forEachField((f, v) -> {
            if(v instanceof Reference r) {
                var r1 = function.apply(r, r.isResolved() && r.resolve().isChildOf(this, f), f.getType());
                if(r1 != r)
                    setField(f, r1);
            }
        });
    }

    public Object getField(List<Id> fieldPath) {
        ensureLoaded();
        var fieldId = fieldPath.get(0);
        InstanceField field = field(fieldId);
        if (fieldPath.size() > 1) {
            var subFieldPath = fieldPath.subList(1, fieldPath.size());
            return NncUtils.get((ClassInstance) ((Reference) field.getValue()).resolve(), inst -> inst.getField(subFieldPath));
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
        return !isInlineValue();
    }

    public Value getInstanceField(Field field) {
        ensureLoaded();
        return field(field).getValue();
    }

    public Set<Instance> getChildren() {
        ensureLoaded();
        var children = new IdentitySet<Instance>();
        forEachField((f, v) -> {
            if (f.isChild()) {
                if (v.isNotNull()) {
                    children.add(((Reference) v).resolve());
                }
            }
        });
        return children;
    }

    @Override
    protected void writeBody(InstanceOutput output) {
        ensureLoaded();
        var subTables = fieldTable.subTables;
        int numKlasses = 0;
        for (FieldSubTable subTable : subTables) {
            if (subTable.countFieldsForWriting() > 0)
                numKlasses++;
        }
        output.writeInt(numKlasses);
        for (FieldSubTable subTable : subTables) {
            int numFields = subTable.countFieldsForWriting();
            if (numFields == 0)
                continue;
            output.writeLong(subTable.klassTag);
            output.writeInt(numFields);
            subTable.forEach(field -> {
                if (!field.shouldSkipWrite()) {
                    output.writeInt(field.getTag());
                    field.writeValue(output);
                }
            });
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
    public boolean isArray() {
        return false;
    }

    @Override
    @NoProxy
    public void readFrom(InstanceInput input) {
        setLoaded(input.isLoadedFromCache());
        fieldTable.initializeFieldsArray();
        var sortedKlasses = klass.getSortedKlasses();
        int j = 0;
        int numKlasses = input.readInt();
        for (int i = 0; i < numKlasses; i++) {
            var groupTag = input.readLong();
            int cmp = 1;
            Klass sk;
            while (j < sortedKlasses.size() && (cmp = Long.compare((sk = sortedKlasses.get(j)).getTag(), groupTag)) < 0) {
                var subTable = fieldTable.addSubTable(sk.getTag());
                for (var field : sk.getSortedFields()) {
                    subTable.add(new InstanceField(this, field, Instances.nullInstance()));
                }
                j++;
            }
            if (cmp == 0) {
                sk = sortedKlasses.get(j++);
                var subTable = fieldTable.addSubTable(groupTag);
                var fields = sk.getSortedFields();
                int m = 0;
                int numFields = input.readInt();
                for (int l = 0; l < numFields; l++) {
                    var fieldTag = input.readInt();
                    Field field;
                    while (m < fields.size() && (field = fields.get(m)).getTag() < fieldTag) {
                        subTable.add(new InstanceField(this, field, Instances.nullInstance()));
                        m++;
                    }
                    if (m < fields.size() && (field = fields.get(m)).getTag() == fieldTag) {
                        input.setParentField(field);
                        var value = input.readValue();
                        subTable.add(new InstanceField(this, field, value));
                        m++;
                    } else
                        subTable.add(new UnknownField(this, groupTag, fieldTag, input.readInstanceBytes()));
                }
                input.setParentField(getParentField());
                for (; m < fields.size(); m++) {
                    var field = fields.get(m);
                    subTable.add(new InstanceField(this, field, Instances.nullInstance()));
                }
            } else {
                var subTable = fieldTable.addSubTable(groupTag);
                int numFields = input.readInt();
                for (int k = 0; k < numFields; k++) {
                    subTable.add(new UnknownField(this, groupTag, input.readInt(), input.readInstanceBytes()));
                }
            }
        }
        for (; j < sortedKlasses.size(); j++) {
            var klass = sortedKlasses.get(j);
            var subTale = fieldTable.addSubTable(klass.getTag());
            for (Field field : klass.getSortedFields()) {
                subTale.add(new InstanceField(this, field, Instances.nullInstance()));
            }
        }
    }

    public ClassInstance getClassInstance(Field field) {
        ensureLoaded();
        return (ClassInstance) ((Reference) field(field).getValue()).resolve();
    }

    public Value getField(String fieldPath) {
        ensureLoaded();
        int idx = fieldPath.indexOf('.');
        if (idx == -1) {
            return getField(klass.getFieldNyName(fieldPath));
        } else {
            String fieldName = fieldPath.substring(0, idx);
            String subPath = fieldPath.substring(idx + 1);
            ClassInstance fieldInstance = (ClassInstance) ((Reference) getInstanceField(fieldName)).resolve();
            return NncUtils.get(fieldInstance, inst -> inst.getField(subPath));
        }
    }

    public Value getInstanceField(String fieldName) {
        ensureLoaded();
        return field(klass.tryGetFieldByName(fieldName)).getValue();
    }

    public void setField(String fieldCode, Value value) {
        var field = klass.getFieldByCode(fieldCode);
        setField(field, value);
    }

    public void setField(Field field, Value value) {
        ensureLoaded();
        setFieldInternal(field, value, true);
    }

    public void setFieldForce(Field field, Value value) {
        setFieldInternal(field, value, false);
    }

    private void setFieldInternal(Field field, Value value, boolean checkMutability) {
        ensureLoaded();
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(klass));
        if (checkMutability && field.isReadonly())
            throw new BusinessException(ErrorCode.CAN_NOT_MODIFY_READONLY_FIELD, field.getQualifiedName());
        if (field.isChild() && value.isNotNull())
            ((Reference) value).resolve().setParent(this, field);
        setModified();
        field(field).set(value);
    }

    public boolean isFieldInitialized(Field field) {
        ensureLoaded();
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(klass));
        return field(field).isFieldInitialized();
    }

    public @Nullable Field findUninitializedField(Klass type) {
        ensureLoaded();
        NncUtils.requireTrue(type.isAssignableFrom(klass));
        return type.findField(f -> !f.isMetadataRemoved() && !isFieldInitialized(f));
    }

    public void initField(Field field, Value value) {
        ensureLoaded();
        initFieldInternal(field, value);
    }

    private void initFieldInternal(Field field, Value value) {
//        try (var ignored = ContextUtil.getProfiler().enter("ClassInstance.initFieldInternal")) {
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(klass));
        NncUtils.requireFalse(isFieldInitialized(field),
                "Field " + field.getQualifiedName() + " is already initialized");
        if (field.isChild() && value.isNotNull())
            ((Reference) value).resolve().setParent(this, field);
        addField(field, value);
//        }
    }

    public StringValue getStringField(Field field) {
        return (StringValue) getField(field(field).getField());
    }

    public LongValue getLongField(Field field) {
        return (LongValue) getField(field);
    }

    public DoubleValue getDoubleField(Field field) {
        return (DoubleValue) getField(field);
    }

    public Value getField(Field field) {
        ensureLoaded();
        return field(field).getValue();
    }

    public void tryClearUnknownField(long klassTag, int tag) {
        fieldTable.tryClearUnknownField(klassTag, tag);
    }

    public Value getUnknownField(long klassTag, int tag) {
        return fieldTable.getUnknown(klassTag, tag);
    }

    public @Nullable Value tryGetUnknown(long klassId, int tag) {
        return fieldTable.tryGetUnknown(klassId, tag);
    }

    public FlowValue getFunction(Method method) {
        ensureLoaded();
        if (functions == null)
            functions = new HashMap<>();
        var concreteFlow = klass.tryResolveMethod(method);
        return functions.computeIfAbsent(concreteFlow,
                k -> new FlowValue(klass.tryResolveMethod(method), this));
    }

    public Value getProperty(Property property) {
        return switch (property) {
            case Field field -> getField(field);
            case Method method -> getFunction(method);
            default -> throw new IllegalStateException("Unexpected value: " + property);
        };
    }

    private InstanceField field(Field field) {
        return fieldTable.get(field);
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
                        NncUtils.map(elements, Value::toFieldValueDTO)
                );
            }
        } else
            return new ClassInstanceParam(NncUtils.filterByTypeAndMap(fieldTable, InstanceField.class, InstanceField::toDTO));
    }

//    @Override
    protected void writeTree(TreeWriter treeWriter) {
        ensureLoaded();
        treeWriter.writeLine(getType().getName() + " " + getTitle());
        treeWriter.indent();
        forEachField((f, v) -> {
            treeWriter.writeLine(f.getName() + ":");
            treeWriter.indent();
            if (v instanceof Reference r && (r.isValueReference() || f.isChild()))
                r.resolve().writeTree(treeWriter);
            else
                treeWriter.writeLine(v.getTitle());
            treeWriter.deIndent();
        });
        treeWriter.deIndent();
    }

    @Override
    public void accept(InstanceVisitor visitor) {
        visitor.visitClassInstance(this);
    }

    //    @Override
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

    public ArrayInstance getInstanceArray(Field field) {
        ensureLoaded();
        return field(field).getInstanceArray();
    }

//    @Override
    public Object toJson(IEntityContext context) {
        if (isList()) {
            var listNative = new ListNative(this);
            var array = listNative.toArray();
            var list = new ArrayList<>();
            array.forEach(e -> list.add(e.toJson(context)));
            return list;
        } else {
            var map = new HashMap<String, Object>();
            forEachField((f, v) -> {
                if (f.isPublic())
                    map.put(f.getCode(), v.toJson(context));
            });
            getKlass().forEachMethod(m -> {
                if (m.isGetter())
                    map.put(m.getPropertyName(), Flows.invokeGetter(m, this, context));
            });
            return map;
        }
    }

    private void ensureFieldInitialized(Field field) {
        if (field(field).isFieldInitialized())
            return;
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

//    @Override
    public boolean isMutable() {
        return getKlass().getKind() != ClassKind.VALUE;
    }

    @Override
    public Instance copy() {
        var copy = ClassInstanceBuilder.newBuilder(getType()).initFieldTable(false).build();
        copy.fieldTable.initializeFieldsArray();
        for (FieldSubTable subTable : fieldTable.subTables) {
            var st = copy.fieldTable.addSubTable(subTable.klassTag);
            for (IInstanceField field : subTable.fields) {
                if(field instanceof InstanceField f) {
                    var v = f.getValue();
                    if(f.getField().isChild() && v instanceof Reference r)
                        v = r.resolve().copy().getReference();
                    st.add(new InstanceField(copy, f.getField(), v));
                }
            }
        }
        return copy;
    }

    public void setUnknown(long classTag, int fieldTag, Value value) {
        var bout = new ByteArrayOutputStream();
        var out = new InstanceOutput(bout);
        out.writeValue(value);
        UnknownField uf = new UnknownField(this, classTag, fieldTag, bout.toByteArray());
        fieldTable.addUnknownField(uf);
    }

    public void setFieldByTag(long classTag, int fieldTag, Value value) {
        var st = NncUtils.find(fieldTable.subTables, s -> s.klassTag == classTag);
        if(st != null) {
            var ff = NncUtils.find(st.fields, f -> f.getTag() == fieldTag);
            if(ff != null) {
                ff.set(value);
                return;
            }
        }
        setUnknown(classTag, fieldTag, value);
    }

    private static class FieldTable implements Iterable<IInstanceField> {

        private final ClassInstance owner;
        private final List<FieldSubTable> subTables = new ArrayList<>();
        private InstanceField[] fields;

        private FieldTable(ClassInstance owner) {
            this.owner = owner;
        }

        void initializeFieldsArray() {
            fields = new InstanceField[owner.klass.getNumFields()];
        }

        void initialize() {
            initializeFieldsArray();
            for (Klass k : owner.klass.getSortedKlasses()) {
                addSubTable(k.getTag()).initialize(owner, k);
            }
        }

        FieldSubTable addSubTable(long klassTag) {
            var subTable = new FieldSubTable(this, klassTag);
            subTables.add(subTable);
            return subTable;
        }

        void onFieldAdded(InstanceField field) {
            fields[field.getField().getOffset()] = field;
        }

        void tryClearUnknownField(long classTag, int tag) {
            for (FieldSubTable subTable : subTables) {
                if(subTable.klassTag == classTag) {
                    subTable.clearUnknown(tag);
                    return;
                }
            }
        }

        InstanceField get(Field field) {
            return fields[field.getOffset()];
        }

        void forEachField(BiConsumer<Field, Value> action) {
            for (InstanceField field : fields) {
                if(!field.getField().isMetadataRemoved())
                    action.accept(field.getField(), field.getValue());
            }
        }

        void clear() {
            for (InstanceField field : fields) {
                field.clear();
            }
        }

        public void forEach(Consumer<? super IInstanceField> action) {
            for (FieldSubTable subTable : subTables) {
                subTable.forEach(action);
            }
        }

        @NotNull
        @Override
        public Iterator<IInstanceField> iterator() {
            var tableIt = subTables.iterator();
            Iterator<IInstanceField> i = null;
            while (tableIt.hasNext()) {
                var n = tableIt.next().iterator();
                if (n.hasNext()) {
                    i = n;
                    break;
                }
            }
            var firstIt = i;
            return new Iterator<>() {

                Iterator<IInstanceField> it = firstIt;

                @Override
                public boolean hasNext() {
                    return it != null;
                }

                @Override
                public IInstanceField next() {
                    var next = it.next();
                    if (!it.hasNext()) {
                        Iterator<IInstanceField> nextIt = null;
                        while (tableIt.hasNext()) {
                            var n = tableIt.next().iterator();
                            if (n.hasNext()) {
                                nextIt = n;
                                break;
                            }
                        }
                        it = nextIt;
                    }
                    return next;
                }
            };
        }

        public Value getUnknown(long klassTag, int tag) {
            var r = tryGetUnknown(klassTag, tag);
            if(r != null)
                return r;
            throw new IllegalStateException("Can not find unknown field " + klassTag + "." + tag + " in " + owner.getId());
        }

        public @Nullable Value tryGetUnknown(long klassTag, int tag) {
            for (var f : this) {
                if(f instanceof UnknownField uf && uf.getKlassTag() == klassTag && uf.getTag() == tag)
                    return uf.getValue();
            }
            return null;
        }

        public void addUnknownField(UnknownField unknownField) {
            var it = subTables.listIterator();
            while (it.hasNext()) {
                var st = it.next();
                if(st.klassTag == unknownField.getKlassTag()) {
                    st.add(unknownField);
                    return;
                }
                else if(st.klassTag > unknownField.getKlassTag()) {
                    it.previous();
                    break;
                }
            }
            var st = new FieldSubTable(this, unknownField.getKlassTag());
            it.add(st);
            st.addUnknownField(unknownField);
        }
    }

    private static class FieldSubTable implements Iterable<IInstanceField> {
        private final FieldTable table;
        private final long klassTag;
        private final List<IInstanceField> fields = new ArrayList<>();

        public FieldSubTable(FieldTable table, long klassTag) {
            this.table = table;
            this.klassTag = klassTag;
        }

        void initialize(ClassInstance owner, Klass klass) {
            for (Field field : klass.getSortedFields()) {
                add(new InstanceField(owner, field));
            }
        }

        void add(IInstanceField field) {
            fields.add(field);
            if (field instanceof InstanceField f)
                table.onFieldAdded(f);
        }

        void addUnknownField(UnknownField unknownField) {
            var index = Collections.binarySearch(fields, unknownField);
            if(index >= 0)
                throw new IllegalStateException("Field " + unknownField.getKlassTag() + "." + unknownField.getTag() + " already exists in the field table");
            fields.add(-(index + 1), unknownField);
        }

        int countFieldsForWriting() {
            int count = 0;
            for (IInstanceField field : fields) {
                if (field != null && !field.shouldSkipWrite())
                    count++;
            }
            return count;
        }

        @Override
        public void forEach(Consumer<? super IInstanceField> action) {
            fields.forEach(action);
        }

        @NotNull
        @Override
        public Iterator<IInstanceField> iterator() {
            return fields.iterator();
        }

        public boolean clearUnknown(int tag) {
            return fields.removeIf(f -> f instanceof UnknownField && f.getTag() == tag);
        }
    }

}