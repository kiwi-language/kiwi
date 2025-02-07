package org.metavm.object.instance.core;

import org.metavm.entity.Identifiable;
import org.metavm.entity.Tree;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.type.Field;
import org.metavm.object.type.Type;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;


public interface Instance extends Message, Identifiable {

    Logger log = LoggerFactory.getLogger(Instance.class);

    default Reference getReference() {
        return new Reference(this);
    }

    default @Nullable Id tryGetOldId() {
        return null;
    }

    default Id getOldId() {
        throw new UnsupportedOperationException();
    }

    void forEachChild(Consumer<? super Instance> action);

    default Id tryGetId() {
        return state().id;
    }

    default Id getId() {
        return requireNonNull(tryGetId(), () -> Instances.getInstancePath(this) + " id not initialized yet");
    }

    default String getStringId() {
        return Utils.safeCall(tryGetId(), Id::toString);
    }

    @Nullable
    default Long tryGetTreeId() {
        var id = state().id;
        return id != null ? id.tryGetTreeId() : null;
    }

    default boolean isRemoved() {
        return state().isRemoved();
    }

    default boolean isPersisted() {
        return !isNew();
    }

    default boolean isNew() {
        return state().isNew();
    }

    default void setRemoved() {
        if (isRemoved())
            throw new InternalException(String.format("Instance %s is already removed", this));
        if (DebugEnv.traceInstanceRemoval)
            log.trace("Removing instance {} {}", this, tryGetId(), new Exception());
        state().setRemoved();
    }

    default long getTreeId() {
        var treeId = tryGetTreeId();
        if (treeId != null)
            return treeId;
        else
            throw new NullPointerException("Instance " + this + " missing ID");
    }

    default boolean idEquals(Id id) {
        var thisId = tryGetId();
        return thisId != null && thisId.equals(id);
    }

    default boolean isIdInitialized() {
        var id = state().id;
        return id != null && !id.isTemporary();
    }

    default void initId(Id id) {
        this.state().id = id;
    }

    default void forEachDescendant(Consumer<Instance> action) {
        action.accept(this);
        forEachChild(c -> c.forEachDescendant(action));
    }

    default void forEachProperDescendant(Consumer<Instance> action) {
        forEachChild(c -> c.forEachDescendant(action));
    }

    default void forEachDescendantConditional(Predicate<Instance> action) {
        if (action.test(this))
            forEachChild(c -> c.forEachDescendantConditional(action));
    }

    default void forEachMember(Consumer<? super Instance> action) {
        forEachChild(action);
        forEachValue(action);
    }

    void forEachValue(Consumer<? super Instance> action);

    Instance getRoot();

    default boolean isRoot() {
        return getRoot() == this;
    }

    default boolean isValue() {
        return false;
    }

    default int getSeq() {
        return state().seq;
    }

    default void setSeq(int seq) {
        this.state().seq = seq;
    }

    default long nextNodeId() {
        return state().nextNodeId++;
    }

    default long getNextNodeId() {
        return state().nextNodeId;
    }

    default void setNextNodeId(long nextNodeId) {
        this.state().nextNodeId = nextNodeId;
    }

    default Tree toTree() {
        Utils.require(isRoot());
        return new Tree(getTreeId(), getVersion(), state().nextNodeId, InstanceOutput.toBytes(this));
    }

    default long getVersion() {
        return state().version;
    }

    default long getSyncVersion() {
        return state().syncVersion;
    }

    Type getInstanceType();

    default boolean isInlineValue() {
        return false;
    }

    default boolean isArray() {
        return false;
    }

    String getTitle();

//    public InstanceDTO toDTO() {
//        return null;
//    }

//    void write(MvOutput output);

    default InstanceParam getParam() {
        return null;
    }

    default boolean isEphemeral() {
        return state().isEphemeral() || getInstanceType().isEphemeral();
    }

    default void setEphemeral() {
        if (state().isEphemeral())
            return;
        if (!isNew())
            throw new IllegalStateException("Can not make a persisted instance ephemeral");
        forEachDescendant(instance -> instance.state().setEphemeral());
    }

    default boolean isDurable() {
        return !isEphemeral();
    }

    default boolean isChildOf(Instance instance, Field f) {
        return false;
    }

    default void setParent(Instance instance, Field field) {
        throw new UnsupportedOperationException();
    }

    default void writeTree(TreeWriter treeWriter) {

    }

    default Instance copy() {
        return null;
    }

    default Field getParentField() {
        return null;
    }

    private boolean isSeparateChild() {
        return false;
    }

    <R> R accept(InstanceVisitor<R> visitor);

    default void transformReference(Function<Reference, Reference> function) {
    }

    void forEachReference(Consumer<Reference> action);

    default void visitGraph(Predicate<Instance> action) {
        visitGraph(action, r -> true, new IdentitySet<>());
    }

    default void visitGraph(Predicate<Instance> action, Predicate<Reference> predicate) {
        visitGraph(action, predicate, new IdentitySet<>());
    }

    default void visitGraph(Predicate<Instance> action, Predicate<Reference> predicate, IdentitySet<Instance> visited) {
        if (DebugEnv.recordPath)
            DebugEnv.path.clear();
        visitGraph0(action, predicate, visited);
    }

    private void visitGraph0(Predicate<Instance> action, Predicate<Reference> predicate, IdentitySet<Instance> visited) {
        if (DebugEnv.recordPath)
            DebugEnv.path.addLast(this.toString());
        if (visited.add(this) && action.test(this)) {
            forEachReference(r -> {
                if (predicate.test(r))
                    r.get().visitGraph0(action, predicate, visited);
            });
        }
        if (DebugEnv.recordPath)
            DebugEnv.path.removeLast();
    }

    default <R> void acceptChildren(InstanceVisitor<R> visitor) {
        forEachChild(c -> c.accept(visitor));
    }

    default boolean setChangeNotified() {
        if (state().isChangeNotified())
            return false;
        state().setChangeNotified();
        return true;
    }

    default boolean setRemovalNotified() {
        if (state().isRemovalNotified())
            return false;
        state().setRemovalNotified();;
        return true;
    }

    default boolean setAfterContextInitIdsNotified() {
        if (state().isAfterContextInitIdsNotified())
            return false;
        state().setAfterContextInitIdsNotified();
        return true;
    }

    void write(MvOutput output);

    default Id getCurrentId() {
        return Objects.requireNonNull(state().id);
    }

    default Id tryGetCurrentId() {
        return state().id;
    }

    default boolean isLoadedFromCache() {
        return false;
    }

    default String getDescription() {
        var id = state().id;
        if (id != null && getTitle().equals(id.toString())) {
            return getInstanceType().getName() + "/" + getTitle();
        } else {
            if (!getTitle().isEmpty()) {
                return getInstanceType().getName() + "/" + getTitle() + "/" + id;
            } else {
                return getInstanceType().getName() + "/" + id;
            }
        }
    }

    default boolean hasPhysicalId() {
        return state().id instanceof PhysicalId;
    }

    InstanceState state();

    default IInstanceContext getContext() {
        return Objects.requireNonNull(state(), () -> getClass().getName() + " returns a null state").context;
    }

    default void setContext(IInstanceContext context) {
        state().context = context;
    }

    default void incVersion() {
        state().incVersion();
    }

    default @Nullable Instance getNext() {
        return state().next;
    }

    default @Nullable Instance getPrev() {
        return state().prev;
    }

    @Nullable Instance getParent();

    default boolean isMarked() {
        return state().isMarked();
    }

    default void setMarked() {
        state().setMarked();
    }

    default void clearMarked() {
        state().clearMarked();
    }

    default void unlink() {
        state().unlink();
    }

    default void insertAfter(Instance instance) {
        state().insertAfter(instance);
    }

    default boolean isRemoving() {
        return state().isRemoving();
    }

    default void setVersion(long version) {
        state().setVersion(version);
    }

    default void setRemoving() {
        state().setRemoving();
    }

    default void clearRemoving() {
        state().clearRemoving();
    }

    String getText();

}