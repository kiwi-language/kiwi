package org.metavm.util;

import org.metavm.entity.TreeTags;
import org.metavm.object.instance.ChangeType;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.persistence.IndexEntryPO;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.ReferencePO;
import org.metavm.object.type.*;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class InstanceInput implements Closeable {

    public static final Function<Id, DurableInstance> UNSUPPORTED_RESOLVER = id -> {
        throw new UnsupportedOperationException();
    };

    public static final Consumer<DurableInstance> UNSUPPORTED_ADD_VALUE = inst -> {
        throw new UnsupportedOperationException();
    };

    public static final TypeDefProvider UNSUPPORTED_TYPE_DEF_PROVIDER = id -> {
        throw new UnsupportedOperationException();
    };

    public static InstanceInput create(byte[] bytes, IInstanceContext context) {
        var bin = new ByteArrayInputStream(bytes);
        if (context == null)
            return new InstanceInput(bin);
        else
            return context.createInstanceInput(bin);
    }

    private final InputStream inputStream;
    private final Function<Id, DurableInstance> resolver;
    private final Consumer<DurableInstance> addValue;
    private final TypeDefProvider typeDefProvider;
    private long treeId;
    @Nullable
    private DurableInstance parent;
    @Nullable
    private Field parentField;
    private boolean loadedFromCache;

    public InstanceInput(InputStream inputStream) {
        this(inputStream, UNSUPPORTED_RESOLVER, UNSUPPORTED_ADD_VALUE, UNSUPPORTED_TYPE_DEF_PROVIDER);
    }

    public InstanceInput(InputStream inputStream,
                         Function<Id, DurableInstance> resolver,
                         Consumer<DurableInstance> addValue,
                         TypeDefProvider typeDefProvider) {
        this.inputStream = inputStream;
        this.resolver = resolver;
        this.addValue = addValue;
        this.typeDefProvider = typeDefProvider;
    }

    public DurableInstance readSingleMessageGrove() {
        readInt();
        return (DurableInstance) readTree();
    }

    public Message readTree() {
        var treeTag = read();
        return switch (treeTag) {
            case TreeTags.DEFAULT -> readMessage();
            case TreeTags.MIGRATED -> readForwardingPointer();
            default -> throw new IllegalStateException("Invalid tree tag: " + treeTag);
        };
    }

    public DurableInstance readMessage() {
        var version = readLong();
        readTreeId();
        var nextNodeId = readLong();
        var separateChild = readBoolean();
        InstanceReference parent = null;
        Field parentField = null;
        boolean pendingChild = false;
        if (separateChild) {
            parent = resolveInstance(readId());
            var fieldId = readId();
            parentField = ((ClassInstance) parent.resolve()).getKlass().findField(f -> f.idEquals(fieldId));
            pendingChild = parentField == null || !parentField.isChild();
        }
        var instance = (DurableInstance) readInstance().resolveDurable();
        instance.setVersion(version);
        instance.setNextNodeId(nextNodeId);
        if (separateChild) {
            instance.setPendingChild(pendingChild);
            instance.setParentInternal(parent.resolve(), parentField, false);
        }
        return instance;
    }

    public ForwardingPointer readForwardingPointer() {
        return new ForwardingPointer(readId(), readId());
    }

    public Instance readInstance() {
        var wireType = read();
        return switch (wireType) {
            case WireTypes.NULL -> new NullInstance(Types.getNullType());
            case WireTypes.DOUBLE -> new DoubleInstance(readDouble(), Types.getDoubleType());
            case WireTypes.STRING -> new StringInstance(readString(), Types.getStringType());
            case WireTypes.LONG -> new LongInstance(readLong(), Types.getLongType());
            case WireTypes.BOOLEAN -> new BooleanInstance(readBoolean(), Types.getBooleanType());
            case WireTypes.TIME -> new TimeInstance(readLong(), Types.getTimeType());
            case WireTypes.PASSWORD -> new PasswordInstance(readString(), Types.getPasswordType());
            case WireTypes.FORWARDED_REFERENCE -> readForwardedReference();
            case WireTypes.REFERENCE -> readReference();
            case WireTypes.RECORD -> readRecord();
            case WireTypes.MIGRATING_RECORD -> readMigratingRecord();
            case WireTypes.VALUE -> readValue();
            default -> throw new IllegalStateException("Invalid wire type: " + wireType);
        };
    }

    private InstanceReference readReference() {
//        var inst = resolveInstance(readId());
//        if(parentField != null && parentField.isChild())
//            inst.setParentInternal(parent, parentField, false);
//        return inst;
        return resolveInstance(readId());
    }

    public InstanceReference readForwardedReference() {
        var ref = resolveInstance(readId());
        ref.setForwarded(true);
        return ref;
    }

    private InstanceReference resolveInstance(Id id) {
        return new InstanceReference(id, () -> resolver.apply(id));
    }

    private final StreamVisitor skipper = new StreamVisitor(this);

    private Instance readRecord() {
        return readRecord(-1L, -1L, false, treeId, readLong());
    }

    private Instance readMigratingRecord() {
        return readRecord(readLong(), readLong(), readBoolean(), treeId, readLong());
    }

    private Instance readForwardingRecord() {
        return readRecord(readLong(), readLong(), false, treeId, readLong());
    }

    private Instance readRecord(long oldTreeId, long oldNodeId, boolean useOldId, long treeId, long nodeId) {
        var type = Type.readType(this, typeDefProvider);
        var id = PhysicalId.of(treeId, nodeId, type);
        var instance = type instanceof ArrayType arrayType ?
                new ArrayInstance(id, arrayType, false, null) :
                ClassInstanceBuilder.newBuilder((ClassType) type).id(id).initFieldTable(false).build();
        if(parent != null)
            instance.setParentInternal(parent, parentField, true);
        if(oldTreeId != -1L) {
            instance.setOldId(PhysicalId.of(oldTreeId, oldNodeId, type));
            instance.setUseOldId(useOldId);
        }
        var oldParent = parent;
        var ref = instance.getReference();
        parent = instance;
        instance.readFrom(this);
        parent = oldParent;
        addValue.accept(instance);
        return ref;
    }

    private Instance readValue() {
        var type = Type.readType(this, typeDefProvider);
        var instance = type instanceof ArrayType arrayType ?
                new ArrayInstance(arrayType) : ClassInstance.allocateEmpty((ClassType) type);
        instance.readFrom(this);
        addValue.accept(instance);
        return instance.getReference();
    }

    public void skipInstance() {
        skipper.visit();
    }

    public byte[] readInstanceBytes() {
        var bout = new ByteArrayOutputStream();
        var copyVisitor = new StreamCopier(inputStream, bout);
        copyVisitor.visit();
        return bout.toByteArray();
    }

    public String readString() {
        int len = readInt();
        byte[] bytes = new byte[len];
        read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public boolean readBoolean() {
        return read() != 0;
    }

    public long readLong() {
        int b = read();
        boolean negative = (b & 1) == 1;
        long v = b >> 1 & 0x3f;
        int shifts = 6;
        for (int i = 0; (b & 0x80) != 0 && i < 10; i++, shifts += 7) {
            b = read();
            v |= (long) (b & 0x7f) << shifts;
        }
        return negative ? -v : v;
    }

    public int readInt() {
        return (int) readLong();
    }

    public TypeTag readTypeTag() {
        return TypeTag.fromCode(read());
    }

    public double readDouble() {
        long l = 0;
        for (int shifts = 0; shifts < 64; shifts += 8)
            l |= (long) read() << shifts;
        return Double.longBitsToDouble(l);
    }

    public int read(byte[] buf) {
        try {
            return inputStream.read(buf);
        } catch (IOException e) {
            throw new InternalException("Failed to read from the underlying input steam", e);
        }
    }

    public int read() {
        try {
            return inputStream.read();
        } catch (IOException e) {
            throw new InternalException("Failed to read from the underlying input steam", e);
        }
    }

    public void setParent(@Nullable DurableInstance parent) {
        this.parent = parent;
    }

    public void setParentField(@Nullable Field parentField) {
        this.parentField = parentField;
    }

    public Id readId() {
        return Id.readId(this);
    }

    public long readTreeId() {
        return treeId = readLong();
    }

    public boolean isLoadedFromCache() {
        return loadedFromCache;
    }

    public InstancePO readInstancePO(long appId) {
        var id = readLong();
        var data = new byte[readInt()];
        read(data);
        return new InstancePO(appId, id, data, 0L, 0L, readLong());
    }

    public IndexEntryPO readIndexEntryPO(long appId) {
        return new IndexEntryPO(appId, readIndexKeyPO(), readId().toBytes());
    }

    public IndexKeyPO readIndexKeyPO() {
        var indexId = readId().toBytes();
        var columns = new byte[IndexKeyPO.MAX_KEY_COLUMNS][];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = new byte[readInt()];
            read(columns[i]);
        }
        return new IndexKeyPO(indexId, columns);
    }

    public <T> List<T> readList(Supplier<T> read) {
        var size = readInt();
        var list = new ArrayList<T>(size);
        for (int i = 0; i < size; i++) {
            list.add(read.get());
        }
        return list;
    }

    public void setLoadedFromCache(boolean loadedFromCache) {
        this.loadedFromCache = loadedFromCache;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    public long getTreeId() {
        return treeId;
    }

    public ReferencePO readReferencePO(long appId) {
        return new ReferencePO(appId, readLong(), readId().toBytes(), readInt());
    }

    public InstanceLog readInstanceLog(long appId) {
        return new InstanceLog(appId, readId(), ChangeType.values()[readInt()], readLong());
    }
}
