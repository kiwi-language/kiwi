package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.TreeTags;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.persistence.IndexEntryPO;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.type.*;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class InstanceInput extends MvInput {

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
    private final RedirectStatusProvider redirectStatusProvider;
    private long treeId;
    @Nullable
    private MvInstance parent;
    private @Nullable KlassDataSlot currentKlassSlot;
    private boolean loadedFromCache;

    public InstanceInput(InputStream in) {
        this(in, UNSUPPORTED_RESOLVER, UNSUPPORTED_ADD_VALUE, UNSUPPORTED_REDIRECTION_SIGNAL_PROVIDER);
    }

    public InstanceInput(InputStream in,
                         Function<Id, Instance> resolver,
                         Consumer<Instance> addValue,
                         RedirectStatusProvider redirectStatusProvider) {
        super(in);
        this.resolver = resolver;
        this.addValue = addValue;
        this.redirectStatusProvider = redirectStatusProvider;
    }

    public Instance readSingleMessageGrove() {
        readInt();
        return (Instance) readTree();
    }

    @Override
    public Message readTree() {
        var treeTag = read();
        return switch (treeTag) {
            case TreeTags.DEFAULT -> readMessage();
            case TreeTags.RELOCATED-> readForwardingPointer();
            case TreeTags.ENTITY -> readEntityMessage();
            default -> throw new IllegalStateException("Unrecognized tree tag: " + treeTag);
        };
    }

    public Entity readEntityMessage() {
        var version = readLong();
        readTreeId();
        var nextNodeId = readLong();
        var entity = readEntity(Entity.class, null);
        entity.setVersion(version);
        entity.setNextNodeId(nextNodeId);
        return entity;
    }

    public MvInstance readMessage() {
        var version = readLong();
        readTreeId();
        var nextNodeId = readLong();
        var separateChild = readBoolean();
        Reference parent = null;
        Field parentField = null;
//        boolean pendingChild = false;
//        if (separateChild) {
//            parent = resolveInstance(readId());
//            var fieldId = readId();
//            parentField = ((ClassInstance) parent.get()).getInstanceKlass().findField(f -> f.idEquals(fieldId));
//            pendingChild = parentField == null || !parentField.isChild();
//        }
        var instance = (MvInstance) readValue().resolveDurable();
        instance.setVersion(version);
        instance.setNextNodeId(nextNodeId);
//        if (separateChild) {
//            instance.setPendingChild(pendingChild);
//            instance.setParentInternal((MvInstance) parent.get(), parentField, false);
//        }
        return instance;
    }

    public ForwardingPointer readForwardingPointer() {
        return new ForwardingPointer(readId(), readId());
    }

    public Value readRemovingInstance() {
        var ref = readInstance();
        ref.get().setRemoving();
        return ref;
    }

    public Reference readReference() {
//        var inst = resolveInstance(readId());
//        if(parentField != null && parentField.isChild())
//            inst.setParentInternal(parent, parentField, false);
//        return inst;
        return resolveInstance(readId());
    }

    private Reference resolveInstance(Id id) {
        return new EntityReference(id, () -> resolver.apply(id));
    }

    private final StreamVisitor skipper = new StreamVisitor(this);

    public Reference readInstance() {
        return readInstance(treeId, readLong());
    }

    private Reference readInstance(long treeId, long nodeId) {
        var type = (ClassType) this.readType();
        var id = PhysicalId.of(treeId, nodeId);
        var instance = ClassInstanceBuilder.newBuilder(type, id).
                initFieldTable(false)
                .isNew(false)
                .parent((MvClassInstance) parent)
                .build();
        instance.setRefcount(readInt());
        var oldParent = parent;
        var ref = instance.getReference();
        parent = instance;
        instance.readRecord(this);
        parent = oldParent;
        addValue.accept(instance);
        return ref;
    }

    public Value readValueInstance() {
        var type = this.readType();
        var instance = type instanceof ArrayType arrayType ?
                new ArrayInstance(arrayType) :
                ClassInstanceBuilder.newBuilder((ClassType) type, type.isValueType() ? null : TmpId.random()).initFieldTable(false).build();
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

    public void setParent(@Nullable MvInstance parent) {
        this.parent = parent;
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
        var copy = new InstanceInput(in, resolver, addValue, redirectStatusProvider);
        copy.parent = parent;
        copy.treeId = treeId;
        copy.currentKlassSlot = currentKlassSlot;
        copy.loadedFromCache = loadedFromCache;
        return copy;
    }

    @Override
    public <T extends Entity> T readEntity(Class<T> klass, Entity parent) {
        var tag = read();
        //noinspection unchecked
        klass = (Class<T>) EntityRegistry.getEntityClass(tag);
        var entity = super.readEntity(klass, parent);
        addValue.accept(entity);
        return entity;
    }
}
