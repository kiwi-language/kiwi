package org.metavm.object.instance.core;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.flow.ClosureContext;
import org.metavm.flow.Flows;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class MvClassInstance extends MvInstance implements ClassInstance {

    public static final Logger logger = LoggerFactory.getLogger(MvClassInstance.class);

    public static final Klass uninitializedKlass = KlassBuilder.newBuilder(new NullId(), "Uninitialized", "Uninitialized").build();

    private final FieldTable fieldTable = new FieldTable(this);
    private Klass klass;
    private final @NotNull MvClassInstance root;
    private final MvClassInstance parent;
    @Setter
    private int refcount;
    private transient Map<MethodRef, FlowValue> functions;
    private final @Nullable ClosureContext closureContext;
    private final List<ClassInstance> children = new ArrayList<>();
    public InstanceField[] fields;
    @Getter
    private boolean initialized;

    public MvClassInstance(Id id,
                           @NotNull ClassType type,
                           long version,
                           long syncVersion,
                           @Nullable MvClassInstance parent,
                           @Nullable Map<Field, ? extends Value> data,
                           boolean ephemeral,
                           boolean initFieldTable,
                           boolean isNew,
                           @Nullable ClosureContext closureContext) {
        super(id, type, version, syncVersion, ephemeral || parent != null && parent.isEphemeral(), isNew);
        this.klass = type.getKlass();
        if (parent != null) {
            this.parent = parent;
            parent.addChild(this);
            root = parent.getRoot();
        } else {
            root = this;
            this.parent = null;
        }
        this.closureContext = closureContext;
        if (klass != uninitializedKlass && initFieldTable)
            fieldTable.initialize();
        if (data != null)
            reset(data, 0L, 0L);
    }

    public MvClassInstance(Id id, Map<Field, Value> data, Klass klass, boolean isNew) {
        this(id, klass.getType(), 0, 0, null, data, false, true, isNew, null);
    }

    public void reset(Map<Field, ? extends Value> data, long version, long syncVersion) {
//        try (var ignored = ContextUtil.getProfiler().enter("ClassInstance.reset")) {
        setModified();
        clear();
        state.setVersion(version);
        state.setSyncVersion(syncVersion);
        klass.forEachField(field -> {
//                try( var ignored1 = ContextUtil.getProfiler().enter("ClassInstance.reset.forEachField")) {
            Value fieldValue = data.get(field);
            if (fieldValue == null || fieldValue.isNull()) {
                fieldValue = field.getDefaultValue();
            }
            initFieldInternal(field, fieldValue);
//                }
        });
    }

    private void clear() {
        fieldTable.clear();
    }

    public void logFields() {
        fieldTable.forEachField((field, value) -> logger.info("Field: {}, Value: {}", field.getName(), value));
    }

    public void logFieldTable() {
        for (InstanceField field : fieldTable) {
            logger.info("Klass tag {}, field tag {}, value {}",
                    field.getKlassTag(), field.getTag(), field.getValue());
        }
    }

    public void forEachField(BiConsumer<Field, Value> action) {
        fieldTable.forEachField(action);
    }

    public Set<IndexKeyRT> getIndexKeys() {
        var keys = new HashSet<IndexKeyRT>();
        getInstanceType().foreachIndex(indexRef -> keys.addAll(indexRef.createIndexKey(this)));
        return keys;
    }

    private void addField(Field field, Value value) {
        fieldTable.get(field).set(value);
    }

    public String getTitle() {
        Field titleField = klass.getTitleField();
        return titleField != null ? field(titleField).getDisplayValue() : getStringId();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        children.forEach(action);
    }

    @Override
    public void forEachMember(Consumer<? super Instance> action) {
        forEachChild(action);
        forEachValue(action);
    }

    @Override
    public void forEachValue(Consumer<? super Instance> action) {
        forEachField((f, v) -> {
            if(v instanceof ValueReference vr)
                action.accept(vr.get());
        });
    }

    public void forEachReference(Consumer<Reference> action) {
        forEachField((f, v) -> {
            if (v instanceof Reference r)
                action.accept(r);
        });
        children.forEach(c -> action.accept(c.getReference()));
    }

    public void forEachReference(BiConsumer<Reference, Boolean> action) {
        forEachField((f, v) -> {
            if(v instanceof Reference r)
                action.accept(r, false);
        });
        children.forEach(c -> action.accept(c.getReference(), true));
    }

    @Override
    public void forEachReference(TriConsumer<Reference, Boolean, Type> action) {
        forEachField((f, v) -> {
            if(v instanceof Reference r)
                action.accept(r, false, f.getType());
        });
        children.forEach(c -> action.accept(c.getReference(), true, c.getInstanceType()));
    }

    @Override
    public void transformReference(TriFunction<Reference, Boolean, Type, Reference> function) {
        forEachField((f, v) -> {
            if(v instanceof EntityReference r) {
                var r1 = function.apply(r, r.isResolved() && r.get().isChildOf(this), f.getType());
                if(r1 != r)
                    setField(f, r1);
            }
        });
    }

    public ClassType getInstanceType() {
        return (KlassType) super.getInstanceType();
    }

    @Override
    public void removeChildrenIf(Predicate<? super ClassInstance> filter) {
        children.removeIf(filter);
    }

    @Override
    protected void writeBody(MvOutput output) {
        var tracing = DebugEnv.traceInstanceIO;
        if (tracing) log.trace("Writing instance {}", getInstanceType().getTypeDesc());
        output.writeInt(fieldTable.countSubTablesForWriting());
        fieldTable.forEachSubTable(subTable -> {
            int numFields;
            MethodRef writeObjectMethod;
            if(subTable instanceof FieldSubTable st && (writeObjectMethod = st.type.getWriteObjectMethod()) != null) {
                output.writeLong(st.klassTag);
                customWrite(st, writeObjectMethod, output);
            } else if ((numFields = subTable.countFieldsForWriting()) > 0) {
                output.writeLong(subTable.getKlassTag());
                defaultWriteFields(subTable, output, numFields);
            }
        });
        writeChildren(output);
    }

    private void customWrite(FieldSubTable subTable, MethodRef writeObjectMethod, MvOutput output) {
        output.writeInt(-1);
        var markingOutput = new MarkingInstanceOutput();
        markingOutput.setCurrent(this);
        markingOutput.setCurrentKlassSlot(subTable);
        var ctx = ContextUtil.getEntityContext();
        var s = ctx.bind(MvObjectOutputStream.create(markingOutput));
        Flows.invoke(
                writeObjectMethod,
                this,
                List.of(s.getReference()),
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

    private void defaultWriteFields(FieldSubTable subTable, MvOutput output, int numFields) {
        output.writeInt(numFields);
        subTable.fields.forEach(field -> {
            if (!field.shouldSkipWrite()) {
//                if(DebugEnv.flag && field instanceof InstanceField f)
//                    logger.debug("Writing field {}", f.getField().getName());
                output.writeInt(field.getTag());
                field.writeValue(output);
            }
        });
    }

    private void writeChildren(MvOutput output) {
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
                    child.write(output);
            }
        }

    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    protected void readBody(InstanceInput input) {
        var tracing = DebugEnv.traceInstanceIO;
        if (tracing) log.trace("Reading instance {} {}", getInstanceType().getTypeDesc(), tryGetId());
        var sortedKlasses = klass.getSortedKlasses();
        int numKlasses = input.readInt();
        var fieldTable = this.fieldTable;
        fieldTable.initialize();
        var subTables = fieldTable.subTables;
        var oldSlot = input.getCurrentKlassSlot();
        var tag2lev = klass.getTag2level();
        var slots = new InstanceInput[sortedKlasses.size()];
        for (int i = 0; i < numKlasses; i++) {
            var klassTag = input.readLong();
            if (tracing) log.trace("Reading klass part {}", klassTag);
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
                int numFields = input.readInt();
                for (int k = 0; k < numFields; k++) {
                    input.readInt();
                    input.readInstanceBytes();
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
        setInitialized();
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
        var s = MvObjectInputStream.create(input);
        ctx.bind(s);
        Flows.invoke(
                readObjectMethod,
                this,
                List.of(s.getReference()),
                ContextUtil.getEntityContext()
        );
    }

    public void defaultRead(InstanceInput input) {
        defaultReadFields(input, (FieldSubTable) requireNonNull(input.getCurrentKlassSlot()));
    }

    private void defaultReadFields(InstanceInput input, FieldSubTable subTable) {
        var klassTag = subTable.klassTag;
        var fields = subTable.fields;
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
                field.secretlySet(input.readValue());
                m++;
            } else
                input.readInstanceBytes();
        }
        for (; m < fields.size(); m++) {
            fields.get(m).ensureInitialized();
        }
    }

    private void readChildren(InstanceInput input) {
        int childrenCount = input.readInt();
        for (int i = 0; i < childrenCount; i++) {
            input.readValue();
        }
    }

    public void setField(Field field, Value value) {
        setFieldInternal(field, value, true);
    }

    public void setFieldForce(Field field, Value value) {
        setFieldInternal(field, value, false);
    }

    private void setFieldInternal(Field field, Value value, boolean checkMutability) {
        Utils.require(field.getDeclaringType().isAssignableFrom(klass),
                () -> "Field " + field.getQualifiedName() + " is not defined in klass " + klass.getTypeDesc());
        if (checkMutability && field.isReadonly())
            throw new BusinessException(ErrorCode.CAN_NOT_MODIFY_READONLY_FIELD, field.getQualifiedName());
        setModified();
        field(field).set(value);
    }

    public boolean isFieldInitialized(Field field) {
                Utils.require(field.getDeclaringType().isAssignableFrom(klass),
                () -> "Field " + field.getName() + " is declared in class " + field.getDeclaringType().getTypeDesc()
                        + " which is not assignable from instance class "
                        + klass.getTypeDesc());
        return field(field).isFieldInitialized();
    }

    public @Nullable Field findUninitializedField(Klass type) {
                Utils.require(type.isAssignableFrom(klass));
        return type.findField(f -> !f.isMetadataRemoved() && !isFieldInitialized(f));
    }

    public void initField(Field field, Value value) {
                initFieldInternal(field, value);
    }

    private void initFieldInternal(Field field, Value value) {
//        try (var ignored = ContextUtil.getProfiler().enter("ClassInstance.initFieldInternal")) {
        Utils.require(field.getDeclaringType().isAssignableFrom(klass));
        Utils.require(!isFieldInitialized(field),
                "Field " + field.getQualifiedName() + " is already initialized");
        addField(field, value);
//        }
    }

    public String getStringField(Field field) {
        return Instances.toJavaString(getField(field(field).getField()));
    }

    public LongValue getLongField(Field field) {
        return (LongValue) getField(field);
    }

    public DoubleValue getDoubleField(Field field) {
        return (DoubleValue) getField(field);
    }

    public Value getField(Field field) {
        return field(field).getValue();
    }

    public FlowValue getFunction(MethodRef method) {
        if (functions == null)
            functions = new HashMap<>();
        var concreteFlow = getInstanceType().findOverride(method);
        return functions.computeIfAbsent(concreteFlow,
                k -> new FlowValue(requireNonNull(getInstanceType().findOverride(method)), this));
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

    public void writeTree(TreeWriter treeWriter) {
        treeWriter.writeLine(getInstanceType().getName() + " " + getTitle() + " " + tryGetId() + " @ " + System.identityHashCode(this));
        treeWriter.indent();
        forEachField((f, v) -> {
            treeWriter.writeLine(f.getName() + ":");
            treeWriter.indent();
            treeWriter.writeLine(v.getTitle());
            treeWriter.deIndent();
        });
        treeWriter.deIndent();
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitClassInstance(this);
    }

    public ArrayInstance getInstanceArray(Field field) {
                return field(field).getInstanceArray();
    }

    @Override
    public boolean isEnum() {
        return ClassInstance.super.isEnum();
    }

    //    @Override
    public Object toJson(IInstanceContext context) {
        var map = new HashMap<String, Object>();
        forEachField((f, v) -> {
            if (f.isPublic())
                map.put(f.getName(), v.toJson());
        });
        getInstanceKlass().forEachMethod(m -> {
            if (m.isGetter())
                map.put(m.getPropertyName(), Flows.invokeGetter(m.getRef(), this, context));
        });
        return map;
    }

    private void ensureFieldInitialized(Field field) {
        if (field(field).isFieldInitialized())
            return;
        throw new BusinessException(
                ErrorCode.FIELD_NOT_INITIALIZED,
                getInstanceType().getName(),
                field.getName()
        );
    }

    public void ensureAllFieldsInitialized() {
                for (Field field : klass.getAllFields()) {
            ensureFieldInitialized(field);
        }
    }

    public Klass getInstanceKlass() {
                return klass;
    }

//    @Override
    public boolean isMutable() {
        return getInstanceKlass().getKind() != ClassKind.VALUE;
    }

    @Override
    public Instance copy(Function<ClassType, Id> idSupplier) {
        return copy(getInstanceType(), idSupplier);
    }

    @Override
    public Instance copy(ClassType type, Function<ClassType, Id> idSupplier) {
        var id = type.isValueType() ? null :
                (isRoot() ? idSupplier.apply(type) : getRoot().nextChildId());
        var parent = type.isValueType() ? null : (MvClassInstance) getParent();
        var copy = ClassInstanceBuilder.newBuilder(type, id).initFieldTable(false)
                .parent(parent)
                .build();
        copy.initializeFieldsArray();
        for (FieldSubTable subTable : fieldTable.subTables) {
            var st = copy.fieldTable.addSubTable(subTable.type);
            for (var field : subTable.fields) {
                var v = field.getValue();
                st.add(new InstanceField(copy, field.getField(), field.getType(), v));
            }
            if (subTable.type.equals(type))
                break;
        }
        return copy;
    }

    private void initializeFieldsArray() {
        fields = new InstanceField[klass.getFieldCount()];
    }

    private static class FieldTable implements Iterable<InstanceField> {

        private final MvClassInstance owner;
        private final List<FieldSubTable> subTables = new ArrayList<>();

        private FieldTable(MvClassInstance owner) {
            this.owner = owner;
        }

        void initialize() {
            owner.initializeFieldsArray();
            owner.getInstanceType().foreachSuperClassTopDown(t -> {
                var st = addSubTable(t);
                st.initialize(owner, t);
            });
        }

        FieldSubTable addSubTable(ClassType type) {
            var subTable = new FieldSubTable(this, type);
            subTables.add(subTable);
            return subTable;
        }

        FieldSubTable findSubTable(long klassTag) {
            for (FieldSubTable subTable : subTables) {
                if(subTable.klassTag == klassTag)
                    return subTable;
            }
            return null;
        }

        void onFieldAdded(InstanceField field) {
            owner.fields[field.getField().getOffset()] = field;
        }

        InstanceField get(Field field) {
            return owner.fields[field.getOffset()];
        }

        void forEachField(BiConsumer<Field, Value> action) {
            for (InstanceField field : owner.fields) {
                if(!field.getField().isMetadataRemoved())
                    action.accept(field.getField(), field.getValue());
            }
        }

        void clear() {
            for (InstanceField field : owner.fields) {
                field.clear();
            }
        }

        public void forEach(Consumer<? super InstanceField> action) {
            forEachSubTable(st -> st.forEach(action));
        }

        public Iterator<FieldSubTable> subTableIterator() {
            return subTables.iterator();
        }

        public int countSubTablesForWriting() {
            int count = 0;
            for (FieldSubTable st : subTables) {
                if(!st.shouldSkipWrite())
                    count++;
            }
            return count;
        }

        public void forEachSubTable(Consumer<? super FieldSubTable> action) {
            subTables.forEach(action);
        }

        @NotNull
        @Override
        public Iterator<InstanceField> iterator() {
            var tableIt = subTableIterator();
            Iterator<InstanceField> i = null;
            while (tableIt.hasNext()) {
                var n = tableIt.next().iterator();
                if (n.hasNext()) {
                    i = n;
                    break;
                }
            }
            var firstIt = i;
            return new Iterator<>() {

                Iterator<InstanceField> it = firstIt;

                @Override
                public boolean hasNext() {
                    return it != null;
                }

                @Override
                public InstanceField next() {
                    var next = it.next();
                    if (!it.hasNext()) {
                        Iterator<InstanceField> nextIt = null;
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

    }

    public boolean isSearchable() {
        return klass.isSearchable();
    }

    @Nullable
    public ClosureContext getClosureContext() {
        return closureContext;
    }

    @Override
    public void addChild(ClassInstance child) {
        children.add(child);
    }

    @Override
    public Map<String, Value> buildSource() {
        var source = new HashMap<String, Value>();
        forEachField((field, value) -> {
            var prefix = "l" + field.getDeclaringType().getLevel() + ".";
            source.put(prefix + field.getColumn().name(), value);
            if (field.getColumn().fuzzyName() != null)
                source.put(prefix + field.getColumn().fuzzyName(), value);
        });
        return source;
    }

    @Override
    protected void writeHead(MvOutput output) {
        super.writeHead(output);
        if (!isValue())
            output.writeInt(refcount);
    }

    @Override
    public void incRefcount(int amount) {
        refcount += amount;
    }

    @Override
    public int getRefcount() {
        return refcount;
    }

    public @Nullable Instance getParent(int index) {
        var v = this.parent;
        for (int i = 0; i < index; i++) {
            v = requireNonNull(v).parent;
        }
        return v;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public MvInstance getParent() {
        return parent;
    }

    @Override
    public @NotNull MvClassInstance getRoot() {
        return root;
    }

    public List<ClassInstance> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void setType(Type type) {
        super.setType(type);
        klass = ((ClassType) type).getKlass();
    }

    public void setInitialized() {
        initialized = true;
    }

    private static class FieldSubTable implements KlassDataSlot, Iterable<InstanceField> {
        private final FieldTable table;
        private final ClassType type;
        @Getter
        private final long klassTag;
        private final List<InstanceField> fields = new ArrayList<>();

        public FieldSubTable(FieldTable table, ClassType type) {
            this.table = table;
            this.type = type;
            this.klassTag = type.getKlassTag();
        }

        void initialize(MvClassInstance owner, ClassType type) {
            for (Field field : type.getKlass().getSortedFields()) {
                try {
                    add(new InstanceField(owner, field, type.getTypeMetadata().getType(field.getTypeIndex())));
                }
                catch (Exception e) {
                    throw new InternalException("Failed to initialize field '" + field.getQualifiedName() + "' for instance: " + table.owner.getId(), e);
                }
            }
        }

        void add(InstanceField field) {
            fields.add(field);
            table.onFieldAdded(field);
        }

        public int countFieldsForWriting() {
            int count = 0;
            for (InstanceField field : fields) {
                if (!field.shouldSkipWrite())
                    count++;
            }
            return count;
        }

        @Override
        public void forEach(Consumer<? super InstanceField> action) {
            fields.forEach(action);
        }

        @NotNull
        @Override
        public Iterator<InstanceField> iterator() {
            return fields.iterator();
       }

        private boolean shouldSkipWrite() {
            return type.getKlass().getWriteObjectMethod() == null && countFieldsForWriting() == 0;
        }
    }

}