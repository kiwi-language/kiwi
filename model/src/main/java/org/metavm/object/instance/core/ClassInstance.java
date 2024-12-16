package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.NoProxy;
import org.metavm.entity.natives.ListNative;
import org.metavm.flow.ClosureContext;
import org.metavm.flow.Flows;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class ClassInstance extends Instance {

    public static final Logger logger = LoggerFactory.getLogger(ClassInstance.class);

    public static final Klass uninitializedKlass = KlassBuilder.newBuilder("Uninitialized", "Uninitialized").build();

    private final FieldTable fieldTable = new FieldTable(this);
    private Klass klass;
    private transient Map<MethodRef, FlowValue> functions;
    private @Nullable ClosureContext closureContext;
    private final List<ClassInstance> children = new ArrayList<>();

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
                         @Nullable Map<Field, Value> data, boolean ephemeral, boolean initFieldTable,
                         @Nullable ClosureContext closureContext) {
        super(id, type, version, syncVersion, ephemeral, load);
        this.klass = type.getKlass();
        this.closureContext = closureContext;
        if (klass != uninitializedKlass && initFieldTable)
            fieldTable.initialize();
        setParentRef(parentRef);
        if (data != null)
            reset(data, 0L, 0L);
    }

    public ClassInstance(Id id, ClassType type, boolean ephemeral, @Nullable Consumer<Instance> load) {
        super(id, type, 0, 0, ephemeral, load);
        this.klass = type.getKlass();
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
        var keys = new HashSet<IndexKeyRT>();
        getType().foreachIndex(indexRef -> keys.addAll(indexRef.createIndexKey(this)));
        return keys;
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
        children.forEach(action);
    }

    @Override
    public void forEachMember(Consumer<Instance> action) {
        forEachField((f, v) -> {
            if(v instanceof Reference r && (f.isChild() || r.isValueReference()))
                action.accept(r.resolve());
        });
        children.forEach(action);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        forEachField((f, v) -> {
            if(v instanceof Reference r)
                action.accept(r);
        });
        children.forEach(c -> action.accept(c.getReference()));
    }

    @Override
    public void forEachReference(BiConsumer<Reference, Boolean> action) {
        forEachField((f, v) -> {
            if(v instanceof Reference r)
                action.accept(r, f.isChild());
        });
        children.forEach(c -> action.accept(c.getReference(), true));
    }

    @Override
    public void forEachReference(TriConsumer<Reference, Boolean, Type> action) {
        forEachField((f, v) -> {
            if(v instanceof Reference r)
                action.accept(r, f.isChild(), f.getType());
        });
        children.forEach(c -> action.accept(c.getReference(), true, c.getType()));
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

    public KlassType getType() {
        ensureLoaded();
        return (KlassType) super.getType();
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
//        if(DebugEnv.flag)
//            logger.debug("Writing instance " + this);
        output.writeInt(fieldTable.countSubTablesForWriting());
        fieldTable.forEachSubTable(subTable -> {
            int numFields;
            MethodRef writeObjectMethod;
            if(subTable instanceof FieldSubTable st && (writeObjectMethod = st.type.getWriteObjectMethod()) != null) {
                output.writeLong(st.klassTag);
                customWrite(st, writeObjectMethod, output);
            }
            else if ((numFields = subTable.countFieldsForWriting()) > 0) {
                output.writeLong(subTable.getKlassTag());
                defaultWriteFields(subTable, output, numFields);
            }
        });
        writeChildren(output);
    }

    private void customWrite(FieldSubTable subTable, MethodRef writeObjectMethod, InstanceOutput output) {
        output.writeInt(-1);
        var markingOutput = new MarkingInstanceOutput();
        markingOutput.setCurrent(this);
        markingOutput.setCurrentKlassSlot(subTable);
        var ctx = ContextUtil.getEntityContext();
        var s = ctx.bind(MvObjectOutputStream.create(markingOutput, ctx));
        var si = ctx.getInstance(s);
        Flows.invoke(
                writeObjectMethod,
                this,
                List.of(si.getReference()),
                ContextUtil.getEntityContext()
        );
        markingOutput.insertBytesSectionIfRequired();
        var blocks = markingOutput.getBlocks();
        output.writeInt(blocks.size());
        blocks.forEach(b -> b.write(output));
        output.write(markingOutput.toByteArray());
    }

    public void defaultWrite(InstanceOutput output) {
        var st = (FieldSubTable) requireNonNull(output.getCurrentKlassSlot());
        defaultWriteFields(st, output, st.countFieldsForWriting());
    }

    private void defaultWriteFields(IFieldSubTable subTable, InstanceOutput output, int numFields) {
        output.writeInt(numFields);
        subTable.forEach(field -> {
            if (!field.shouldSkipWrite()) {
//                if(DebugEnv.flag && field instanceof InstanceField f)
//                    logger.debug("Writing field {}", f.getField().getName());
                output.writeInt(field.getTag());
                field.writeValue(output);
            }
        });
    }

    private void writeChildren(InstanceOutput output) {
        if(children.isEmpty())
            output.writeInt(0);
        else {
            int childrenCount = 0;
            for (ClassInstance child : children) {
                if (!child.isEphemeral())
                    childrenCount++;
            }
            output.writeInt(childrenCount);
            for (ClassInstance child : children) {
                if(!child.isEphemeral())
                    child.writeRecord(output);
            }
        }

    }

    @Override
    @NoProxy
    public void setType(Type type) {
        if (type instanceof ClassType classType) {
            klass = classType.getKlass();
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
    protected void readFrom(InstanceInput input) {
        var sortedKlasses = klass.getSortedKlasses();
        int numKlasses = input.readInt();
        var fieldTable = this.fieldTable;
        fieldTable.initialize();
        var subTables = fieldTable.subTables;
        var unknownSubTables = fieldTable.unknownSubTables;
        unknownSubTables.clear();
        var oldSlot = input.getCurrentKlassSlot();
        var tag2lev = klass.getTag2level();
        var slots = new InstanceInput[sortedKlasses.size()];
        for (int i = 0; i < numKlasses; i++) {
            var klassTag = input.readLong();
            var lev = tag2lev.get(klassTag);
            FieldSubTable st;
            if(lev != null) {
                st = subTables.get(lev);
                input.setCurrentKlassSlot(st);
                if(st.type.getKlass().getReadObjectMethod() == null) {
                    defaultReadFields(input, st);
                    slots[lev] = input;
                }
                else
                    slots[lev] = input.copy(new ByteArrayInputStream(readSlot(input)));
            } else {
                var ust = new UnknownFieldSubTable(klassTag);
                unknownSubTables.add(ust);
                int numFields = input.readInt();
                for (int k = 0; k < numFields; k++) {
                    ust.unknownFields.add(new UnknownField(this, klassTag, input.readInt(), input.readInstanceBytes()));
                }
            }
        }
        input.setCurrentKlassSlot(oldSlot);
        for (int i = 0; i < slots.length; i++) {
            var slot = slots[i];
            if(slot == input)
                continue;
            var st = subTables.get(i);
            if(slot == null) {
                for (InstanceField field : st.fields) {
                    field.ensureInitialized();
                }
            } else
                customRead(requireNonNull(st.type.getReadObjectMethod()), slot);
        }
        readChildren(input);
    }

    private byte[] readSlot(InstanceInput input) {
        var bout = new ByteArrayOutputStream();
        var copier = new StreamCopier(input.getIn(), bout);
        copier.visitClassBody();
        return bout.toByteArray();
    }

    private void customRead(MethodRef readObjectMethod, InstanceInput input) {
        var flag = input.readInt();
        assert flag == -1;
        var numBlocks = input.readInt();
        for (int i = 0; i < numBlocks; i++) {
            MarkingInstanceOutput.Block.read(input);
        }
        var ctx = ContextUtil.getEntityContext();
        var s = MvObjectInputStream.create(input, ctx);
        ctx.bind(s);
        var si = ctx.getInstance(s);
        Flows.invoke(
                readObjectMethod,
                this,
                List.of(si.getReference()),
                ContextUtil.getEntityContext()
        );
    }

    public void defaultRead(InstanceInput input) {
        defaultReadFields(input, (FieldSubTable) requireNonNull(input.getCurrentKlassSlot()));
    }

    private void defaultReadFields(InstanceInput input, FieldSubTable subTable) {
        var klassTag = subTable.klassTag;
        var fields = subTable.fields;
        var unknownFields = subTable.unknownFields;
        unknownFields.clear();
        int m = 0;
        int numFields = input.readInt();
        for (int l = 0; l < numFields; l++) {
            var fieldTag = input.readInt();
            InstanceField field;
            while (m < fields.size() && (field = fields.get(m)).getTag() < fieldTag) {
                field.ensureInitialized();
                m++;
            }
            if (m < fields.size() && (field = fields.get(m)).getTag() == fieldTag) {
                input.setParentField(field.getField());
                field.secretlySet(input.readValue());
                m++;
            } else
                unknownFields.add(new UnknownField(this, klassTag, fieldTag, input.readInstanceBytes()));
        }
        input.setParentField(getParentField());
        for (; m < fields.size(); m++) {
            fields.get(m).ensureInitialized();
        }
    }

    private void readChildren(InstanceInput input) {
        int childrenCount = input.readInt();
        for (int i = 0; i < childrenCount; i++) {
            children.add(input.readValue().resolveObject());
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
        return field(klass.findFieldByName(fieldName)).getValue();
    }

    public void setField(String fieldCode, Value value) {
        var field = klass.getFieldByName(fieldCode);
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
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(klass),
                () -> "Field " + field.getQualifiedName() + " is not defined in klass " + klass.getTypeDesc()
                        + " with super type " + klass.getSuperType() + ", super equals: "
                        + (field.getDeclaringType() == requireNonNull(klass.getSuperType()).getKlass()));
        if (checkMutability && field.isReadonly())
            throw new BusinessException(ErrorCode.CAN_NOT_MODIFY_READONLY_FIELD, field.getQualifiedName());
        if (field.isChild() && value.isNotNull())
            ((Reference) value).resolve().setParent(this, field);
        setModified();
        field(field).set(value);
    }

    public boolean isFieldInitialized(Field field) {
        ensureLoaded();
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(klass),
                () -> "Field " + field.getName() + " is declared in class " + field.getDeclaringType().getTypeDesc()
                        + " which is not assignable from instance class "
                        + klass.getTypeDesc());
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

    public FlowValue getFunction(MethodRef method) {
        ensureLoaded();
        if (functions == null)
            functions = new HashMap<>();
        var concreteFlow = getType().findOverride(method);
        return functions.computeIfAbsent(concreteFlow,
                k -> new FlowValue(requireNonNull(getType().findOverride(method)), this));
    }

    public Value getProperty(PropertyRef property) {
        return switch (property) {
            case FieldRef field -> getField(field.getRawField());
            case MethodRef method -> getFunction(method);
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
                    requireNonNull(this.getStringIdForDTO(), "Id required"),
                    getType().toExpression());
        }
    }

    public boolean isList() {
        return klass.isList();
    }

    public boolean isEnum() {
        return klass.isEnum();
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
                    map.put(f.getName(), v.toJson(context));
            });
            getKlass().forEachMethod(m -> {
                if (m.isGetter())
                    map.put(m.getPropertyName(), Flows.invokeGetter(m.getRef(), this, context));
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
            var st = copy.fieldTable.addSubTable(subTable.type);
            for (var field : subTable.fields) {
                var v = field.getValue();
                if(field.getField().isChild() && v instanceof Reference r)
                    v = r.resolve().copy().getReference();
                st.add(new InstanceField(copy, field.getField(), field.getType(), v));
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
        var st = fieldTable.findSubTable(classTag);
        if(st != null) {
            var ff = NncUtils.find(st, f -> f.getTag() == fieldTag);
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
        private final List<UnknownFieldSubTable> unknownSubTables = new ArrayList<>();
        private InstanceField[] fields;

        private FieldTable(ClassInstance owner) {
            this.owner = owner;
        }

        void initializeFieldsArray() {
            fields = new InstanceField[owner.klass.getNumFields()];
        }

        void initialize() {
            initializeFieldsArray();
            owner.getType().foreachSuperClassTopDown(t -> {
                var st = addSubTable(t);
                st.initialize(owner, t);
            });
        }

        FieldSubTable addSubTable(ClassType type) {
            var subTable = new FieldSubTable(this, type);
            subTables.add(subTable);
            return subTable;
        }

        IFieldSubTable findSubTable(long klassTag) {
            for (FieldSubTable subTable : subTables) {
                if(subTable.klassTag == klassTag)
                    return subTable;
            }
            for (UnknownFieldSubTable unknownSubTable : unknownSubTables) {
                if(unknownSubTable.klassTag == klassTag)
                    return unknownSubTable;
            }
            return null;
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
            for (UnknownFieldSubTable unknownSubTable : unknownSubTables) {
                if(unknownSubTable.klassTag == classTag) {
                    unknownSubTable.clearUnknown(tag);
                    return;
                }
            }
        }

        void addUnknownSubTable(UnknownFieldSubTable ust) {
            var index = Collections.binarySearch(unknownSubTables, ust);
            if(index >= 0)
                throw new IllegalStateException("FieldSubTable " + ust.getKlassTag() + " already exists");
            unknownSubTables.add(-(index + 1), ust);
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
            forEachSubTable(st -> st.forEach(action));
        }

        public Iterator<IFieldSubTable> subTableIterator() {
            return NncUtils.mergeIterator(subTables.iterator(), unknownSubTables.iterator());
        }

        public int countSubTablesForWriting() {
            int count = 0;
            for (FieldSubTable st : subTables) {
                if(!st.shouldSkipWrite())
                    count++;
            }
            for (UnknownFieldSubTable ust : unknownSubTables) {
                if(!ust.shouldSkipWrite())
                    count++;
            }
            return count;
        }

        public void forEachSubTable(Consumer<? super IFieldSubTable> action) {
            NncUtils.forEachMerged(subTables, unknownSubTables, action);
        }

        @NotNull
        @Override
        public Iterator<IInstanceField> iterator() {
            var tableIt = subTableIterator();
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
            for (FieldSubTable st : subTables) {
                if (st.klassTag == unknownField.getKlassTag()) {
                    st.addUnknownField(unknownField);
                    return;
                } else if (st.klassTag > unknownField.getKlassTag()) {
                    break;
                }
            }
            var ust = new UnknownFieldSubTable(unknownField.getKlassTag());
            addUnknownSubTable(ust);
            ust.add(unknownField);
        }
    }

    public boolean isSearchable() {
        return klass.isSearchable();
    }

    @Nullable
    public ClosureContext getClosureContext() {
        return closureContext;
    }

    public void addChild(ClassInstance child) {
        children.add(child);
        child.setParent(this, null);
    }

    private interface IFieldSubTable extends Iterable<IInstanceField>, Comparable<IFieldSubTable> {

        long getKlassTag();

        @Override
        default int compareTo(@NotNull ClassInstance.IFieldSubTable o) {
            return Long.compare(getKlassTag(), o.getKlassTag());
        }

        int countFieldsForWriting();
    }

    private static class FieldSubTable implements IFieldSubTable, KlassDataSlot {
        private final FieldTable table;
        private final ClassType type;
        private final long klassTag;
        private final List<InstanceField> fields = new ArrayList<>();
        private final List<UnknownField> unknownFields = new ArrayList<>();

        public FieldSubTable(FieldTable table, ClassType type) {
            this.table = table;
            this.type = type;
            this.klassTag = type.getKlassTag();
        }

        void initialize(ClassInstance owner, ClassType type) {
            for (Field field : type.getKlass().getSortedFields()) {
                add(new InstanceField(owner, field, type.getTypeMetadata().getType(field.getTypeIndex())));
            }
        }

        void add(InstanceField field) {
            fields.add(field);
            table.onFieldAdded(field);
        }

        void addUnknownField(UnknownField unknownField) {
            var index = Collections.binarySearch(unknownFields, unknownField);
            if(index >= 0)
                throw new IllegalStateException("Field " + unknownField.getKlassTag() + "." + unknownField.getTag() + " already exists in the field table");
            unknownFields.add(-(index + 1), unknownField);
        }

        public int countFieldsForWriting() {
            int count = 0;
            for (InstanceField field : fields) {
                if (!field.shouldSkipWrite())
                    count++;
            }
            for (UnknownField unknownField : unknownFields) {
                if(!unknownField.shouldSkipWrite())
                    count++;
            }
            return count;
        }

        @Override
        public void forEach(Consumer<? super IInstanceField> action) {
            NncUtils.forEachMerged(fields, unknownFields, action);
        }

        @NotNull
        @Override
        public Iterator<IInstanceField> iterator() {
            return NncUtils.mergeIterator(fields.iterator(), unknownFields.iterator());
       }

        public boolean clearUnknown(int tag) {
//            return fields.removeIf(f -> f instanceof UnknownField && f.getTag() == tag);
            return unknownFields.removeIf(f -> f.getTag() == tag);
        }

        @Override
        public long getKlassTag() {
            return klassTag;
        }

        private boolean shouldSkipWrite() {
            return type.getKlass().getWriteObjectMethod() == null && countFieldsForWriting() == 0;
        }
    }

    private static class UnknownFieldSubTable implements IFieldSubTable, KlassDataSlot {

        private final long klassTag;
        private final List<UnknownField> unknownFields = new ArrayList<>();

        private UnknownFieldSubTable(long klassTag) {
            this.klassTag = klassTag;
        }

        @NotNull
        @Override
        public Iterator<IInstanceField> iterator() {
            //noinspection unchecked,rawtypes
            return (Iterator) unknownFields.iterator();
        }

        void add(UnknownField unknownField) {
            unknownFields.add(unknownField);
        }

        @Override
        public long getKlassTag() {
            return klassTag;
        }

        private boolean shouldSkipWrite() {
            return countFieldsForWriting() == 0;
        }

        public boolean clearUnknown(int tag) {
//            return fields.removeIf(f -> f instanceof UnknownField && f.getTag() == tag);
            return unknownFields.removeIf(f -> f.getTag() == tag);
        }

        public int countFieldsForWriting() {
            int count = 0;
            for (UnknownField unknownField : unknownFields) {
                if(!unknownField.shouldSkipWrite())
                    count++;
            }
            return count;
        }

    }

}