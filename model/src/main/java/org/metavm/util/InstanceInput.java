package org.metavm.util;

import org.metavm.entity.TreeTags;
import org.metavm.flow.Lambda;
import org.metavm.flow.Method;
import org.metavm.object.instance.ChangeType;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.persistence.IndexEntryPO;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.ReferencePO;
import org.metavm.object.type.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

public class InstanceInput extends MvInput {

    public static final Logger logger = LoggerFactory.getLogger(InstanceInput.class);

    public static final Function<Id, Instance> UNSUPPORTED_RESOLVER = id -> {
        throw new UnsupportedOperationException();
    };

    public static final Consumer<Instance> UNSUPPORTED_ADD_VALUE = inst -> {
        throw new UnsupportedOperationException();
    };

    public static final TypeDefProvider UNSUPPORTED_TYPE_DEF_PROVIDER = id -> {
        throw new UnsupportedOperationException();
    };

    public static final RedirectStatusProvider UNSUPPORTED_REDIRECTION_SIGNAL_PROVIDER = id -> {
        throw new UnsupportedOperationException();
    };

    public static InstanceInput create(byte[] bytes, IInstanceContext context) {
        var bin = new ByteArrayInputStream(bytes);
        if (context == null)
            return new InstanceInput(bin);
        else
            return context.createInstanceInput(bin);
    }

    private final Function<Id, Instance> resolver;
    private final Consumer<Instance> addValue;
    private final TypeDefProvider typeDefProvider;
    private final RedirectStatusProvider redirectStatusProvider;
    private long treeId;
    @Nullable
    private Instance parent;
    @Nullable
    private Field parentField;
    private @Nullable KlassDataSlot currentKlassSlot;
    private boolean loadedFromCache;

    public InstanceInput(InputStream in) {
        this(in, UNSUPPORTED_RESOLVER, UNSUPPORTED_ADD_VALUE, UNSUPPORTED_TYPE_DEF_PROVIDER, UNSUPPORTED_REDIRECTION_SIGNAL_PROVIDER);
    }

    public InstanceInput(InputStream in,
                         Function<Id, Instance> resolver,
                         Consumer<Instance> addValue,
                         TypeDefProvider typeDefProvider,
                         RedirectStatusProvider redirectStatusProvider) {
        super(in);
        this.resolver = resolver;
        this.addValue = addValue;
        this.typeDefProvider = typeDefProvider;
        this.redirectStatusProvider = redirectStatusProvider;
    }

    public Instance readSingleMessageGrove() {
        readInt();
        return (Instance) readTree();
    }

    public Message readTree() {
        var treeTag = read();
        return switch (treeTag) {
            case TreeTags.DEFAULT -> readMessage();
            case TreeTags.RELOCATED -> readForwardingPointer();
            default -> throw new IllegalStateException("Invalid tree tag: " + treeTag);
        };
    }

    public Instance readMessage() {
        var version = readLong();
        readTreeId();
        var nextNodeId = readLong();
        var separateChild = readBoolean();
        Reference parent = null;
        Field parentField = null;
        boolean pendingChild = false;
        if (separateChild) {
            parent = resolveInstance(readId());
            var fieldId = readId();
            parentField = ((ClassInstance) parent.resolve()).getKlass().findField(f -> f.idEquals(fieldId));
            pendingChild = parentField == null || !parentField.isChild();
        }
        var instance = (Instance) readValue().resolveDurable();
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

    public Value readValue() {
        var wireType = read();
        return switch (wireType) {
            case WireTypes.NULL -> new NullValue(Types.getNullType());
            case WireTypes.DOUBLE -> new DoubleValue(readDouble(), Types.getDoubleType());
            case WireTypes.STRING -> new StringValue(readUTF(), Types.getStringType());
            case WireTypes.LONG -> new LongValue(readLong(), Types.getLongType());
            case WireTypes.INT -> new IntValue(readInt(), Types.getIntType());
            case WireTypes.CHAR -> new CharValue(readChar(), Types.getCharType());
            case WireTypes.BOOLEAN -> new BooleanValue(readBoolean(), Types.getBooleanType());
            case WireTypes.TIME -> new TimeValue(readLong(), Types.getTimeType());
            case WireTypes.PASSWORD -> new PasswordValue(readUTF(), Types.getPasswordType());
            case WireTypes.FLAGGED_REFERENCE -> readFlaggedReference();
            case WireTypes.REFERENCE -> readReference();
            case WireTypes.REDIRECTING_REFERENCE -> readRedirectingReference();
            case WireTypes.REDIRECTING_INSTANCE -> readRedirectingInstance();
            case WireTypes.INSTANCE -> readInstance();
            case WireTypes.RELOCATING_INSTANCE -> readRelocatingInstance();
            case WireTypes.VALUE_INSTANCE -> readValueInstance();
            case WireTypes.REMOVING_INSTANCE ->  readRemovingInstance();
            default -> throw new IllegalStateException("Invalid wire type: " + wireType);
        };
    }

    private Value readRemovingInstance() {
        var ref = readInstance();
        ref.resolve().setRemoving(true);
        return ref;
    }

    private Value readRedirectingInstance() {
        var redirectionRef = (Reference) readValue();
        var redirectionStatus = redirectStatusProvider.getRedirectStatus(readId());
        var record = (Reference) readValue();
        return new RedirectingReference(record.resolve(), redirectionRef, redirectionStatus);
    }

    private Reference readReference() {
//        var inst = resolveInstance(readId());
//        if(parentField != null && parentField.isChild())
//            inst.setParentInternal(parent, parentField, false);
//        return inst;
        return resolveInstance(readId());
    }

    public Reference readFlaggedReference() {
        var flags = read();
        var ref = resolveInstance(readId());
        ref.setFlags(flags);
        return ref;
    }

    public Reference readRedirectingReference() {
        var flags = read();
        var id = readId();
        var redirectionRef = (Reference) readValue();
        var status = redirectStatusProvider.getRedirectStatus(readId());
        var ref = new RedirectingReference(id, () -> resolver.apply(id), redirectionRef, status);
        ref.setFlags(flags);
        return ref;
    }

    private Reference resolveInstance(Id id) {
        return new Reference(id, () -> resolver.apply(id));
    }

    private final StreamVisitor skipper = new StreamVisitor(this);

    private Reference readInstance() {
        return readInstance(-1L, -1L, false, treeId, readLong());
    }

    private Value readRelocatingInstance() {
        return readInstance(readLong(), readLong(), readBoolean(), treeId, readLong());
    }

    private Reference readInstance(long oldTreeId, long oldNodeId, boolean useOldId, long treeId, long nodeId) {
        var type = Type.readType(this);
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
        instance.readRecord(this);
        parent = oldParent;
        addValue.accept(instance);
        return ref;
    }

    private Value readValueInstance() {
        var type = Type.readType(this);
        var instance = type instanceof ArrayType arrayType ?
                new ArrayInstance(arrayType) : ClassInstance.allocateEmpty((ClassType) type);
        instance.readRecord(this);
        addValue.accept(instance);
        return instance.getReference();
    }

    public void skipInstance() {
        skipper.visitValue();
    }

    public byte[] readInstanceBytes() {
        var bout = new ByteArrayOutputStream();
        var copyVisitor = new StreamCopier(getIn(), bout);
        copyVisitor.visitValue();
        return bout.toByteArray();
    }

    @Override
    public Klass getKlass(Id id) {
        return (Klass) typeDefProvider.getTypeDef(id);
    }

    @Override
    public Method getMethod(Id id) {
        return (Method) typeDefProvider.getTypeDef(id);
    }

    @Override
    public Field getField(Id id) {
        return (Field) typeDefProvider.getTypeDef(id);
    }

    @Override
    public TypeVariable getTypeVariable(Id id) {
        return (TypeVariable) typeDefProvider.getTypeDef(id);
    }

    @Override
    public org.metavm.flow.Function getFunction(Id id) {
        return (org.metavm.flow.Function) typeDefProvider.getTypeDef(id);
    }

    @Override
    public CapturedTypeVariable getCapturedTypeVariable(Id id) {
        return (CapturedTypeVariable) typeDefProvider.getTypeDef(id);
    }

    @Override
    public Lambda getLambda(Id id) {
        return (Lambda) typeDefProvider.getTypeDef(id);
    }

    @Override
    public Index getIndex(Id id) {
        return (Index) typeDefProvider.getTypeDef(id);
    }

    @Override
    public IndexField getIndexField(Id id) {
        return (IndexField) typeDefProvider.getTypeDef(id);
    }

    @Override
    public EnumConstantDef getEnumConstantDef(Id id) {
        return (EnumConstantDef) typeDefProvider.getTypeDef(id);
    }

    public void setParent(@Nullable Instance parent) {
        this.parent = parent;
    }

    public void setParentField(@Nullable Field parentField) {
        this.parentField = parentField;
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
        var len = readInt();
        var data = new byte[len];
        read(data);
        return new IndexKeyPO(indexId, data);
    }

    public void setLoadedFromCache(boolean loadedFromCache) {
        this.loadedFromCache = loadedFromCache;
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

    @Nullable
    public Instance getParent() {
        return parent;
    }

    @Nullable
    public KlassDataSlot getCurrentKlassSlot() {
        return currentKlassSlot;
    }

    public void setCurrentKlassSlot(@Nullable KlassDataSlot currentKlassSlot) {
        this.currentKlassSlot = currentKlassSlot;
    }

    public InstanceInput copy(InputStream in) {
        var copy = new InstanceInput(in, resolver, addValue, typeDefProvider, redirectStatusProvider);
        copy.parent = parent;
        copy.parentField = parentField;
        copy.treeId = treeId;
        copy.currentKlassSlot = currentKlassSlot;
        copy.loadedFromCache = loadedFromCache;
        return copy;
    }

}
