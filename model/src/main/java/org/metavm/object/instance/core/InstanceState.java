package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@Slf4j
public class InstanceState {

    public static InstanceState ephemeral(Instance instance) {
        return new InstanceState(null, 0L, 0L, true, false, instance);
    }

    @Nullable
    Instance prev;
    @Nullable
    Instance next;
    public final Id id;
    long version;
    long syncVersion;
    @NotNull Instance root;
    long nextNodeId = 1;
    Instance instance;
    @Nullable MvInstance parent;
    IInstanceContext context;

    // flags TODO use bitset

    public long flags;

    public static final int FLAG_REMOVED = 1;
    public static final int FLAG_REMOVING = 2;
    public static final int FLAG_EPHEMERAL = 4;
    public static final int FLAG_STRICT_EPHEMERAL = 8;
    public static final int FLAG_CHANGE_NOTIFIED = 16;
    public static final int FLAG_REMOVAL_NOTIFIED = 32;
    public static final int FLAG_MARKED = 64;
    public static final int FLAG_AFTER_CONTEXT_INIT_IDS_NOTIFIED = 128;
    public static final int FLAG_NEW = 256;

    public InstanceState(@Nullable Id id, long version, long syncVersion, boolean ephemeral, boolean isNew, Instance instance) {
        this.version = version;
        this.syncVersion = syncVersion;
        if (ephemeral)
            setEphemeral();
        this.id = id;
        if(isNew) setNew();
        this.root = this.instance = instance;
    }

    void unlink() {
        Instance next = this.next, prev = this.prev;
        if (prev != null)
            prev.state().next = next;
        if (next != null)
            next.state().prev = prev;
        this.next = this.prev = null;
    }

    void insertAfter(Instance instance) {
        var next = this.next;
        instance.state().next = next;
        if (next != null)
            next.state().prev = instance;
        this.next = instance;
        instance.state().prev = instance;
    }

    public void incVersion() {
        version++;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setSyncVersion(long syncVersion) {
        this.syncVersion = syncVersion;
    }

    public @Nullable Instance getParent() {
        return parent;
    }

    public Instance getRoot() {
        if (root == instance)
            return instance;
        else
            return root = root.getRoot();
    }

    public boolean isRemoving() {
        return testFlag(FLAG_REMOVING);
    }

    public void setRemoving() {
        setFlag(FLAG_REMOVING);
    }

    public void clearRemoving() {
        clearFlag(FLAG_REMOVING);
    }

    public boolean isRemoved() {
        return testFlag(FLAG_REMOVED);
    }

    public void setRemoved() {
        setFlag(FLAG_REMOVED);
    }

    public boolean isEphemeral() {
        return testFlag(FLAG_EPHEMERAL);
    }

    public void setEphemeral() {
        setFlag(FLAG_EPHEMERAL);
    }

    public boolean isStrictEphemeral() {
        return testFlag(FLAG_STRICT_EPHEMERAL);
    }

    public void setStrictEphemeral() {
        setFlag(FLAG_STRICT_EPHEMERAL);
    }

    public boolean isChangeNotified() {
        return testFlag(FLAG_CHANGE_NOTIFIED);
    }

    public void setChangeNotified() {
        setFlag(FLAG_CHANGE_NOTIFIED);
    }

    public boolean isRemovalNotified() {
        return testFlag(FLAG_REMOVAL_NOTIFIED);
    }

    public void setRemovalNotified() {
        setFlag(FLAG_REMOVAL_NOTIFIED);
    }

    public boolean isAfterContextInitIdsNotified() {
        return testFlag(FLAG_AFTER_CONTEXT_INIT_IDS_NOTIFIED);
    }

    public void setAfterContextInitIdsNotified() {
        setFlag(FLAG_AFTER_CONTEXT_INIT_IDS_NOTIFIED);
    }

    public boolean isNew() {
        return testFlag(FLAG_NEW);
    }

    public void setNew() {
        setFlag(FLAG_NEW);
    }

    public boolean isMarked() {
        return testFlag(FLAG_MARKED);
    }

    public void setMarked() {
        setFlag(FLAG_MARKED);
    }

    public void clearMarked() {
        clearFlag(FLAG_MARKED);
    }

    private boolean testFlag(int flag) {
        return (flags & flag) != 0;
    }

    private void setFlag(int flag) {
        flags |= flag;
    }

    private void clearFlag(int flag) {
        flags &= ~flag;
    }

}
