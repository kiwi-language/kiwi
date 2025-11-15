package org.metavm.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.metavm.wire.AdapterRegistry;
import org.metavm.entity.Entity;
import org.metavm.wire.WireAdapter;
import org.metavm.entity.TreeTags;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.RedirectStatusProvider;

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
    @Getter
    private long treeId;
    @Nullable
    private MvInstance parent;
    private @Nullable KlassDataSlot currentKlassSlot;
    @Getter
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
        var entity = (Entity) readEntity();
        entity.setVersion(version);
        entity.setNextNodeId(nextNodeId);
        return entity;
    }

    public MvInstance readMessage() {
        var version = readLong();
        readTreeId();
        var nextNodeId = readLong();
        readBoolean();
        var instance = (MvInstance) readValue().resolveDurable();
        instance.setVersion(version);
        instance.setNextNodeId(nextNodeId);
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
        return resolveInstance(readId());
    }

    private Reference resolveInstance(Id id) {
        return new EntityReference(id, () -> resolver.apply(id));
    }

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

    /** @noinspection UnusedReturnValue*/
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
    public Object readEntity(Object parent) {
        var tag = read();
        var adapter = AdapterRegistry.instance.getAdapter(tag);
        return readEntity0(adapter, parent);
    }

    @Override
    public <T> T readEntity(WireAdapter<T> adapter, @org.jetbrains.annotations.Nullable Object parent) {
        if (adapter.getTag() != -1) {
            var tag = read();
            assert tag == adapter.getTag();
        }
        return readEntity0(adapter, parent);
    }

    private <T> T readEntity0(WireAdapter<T> adapter, Object parent) {
        var entity = super.readEntity(adapter, parent);
        if (entity instanceof Instance inst)
            addValue.accept(inst);
        return  entity;
    }

}
