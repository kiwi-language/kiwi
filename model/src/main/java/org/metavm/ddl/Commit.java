package org.metavm.ddl;

import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.entity.IndexDef;
import org.metavm.entity.ReadWriteArray;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.RedirectStatus;

import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;

@Entity
public class Commit extends org.metavm.entity.Entity implements RedirectStatus {

    public static final IndexDef<Commit> IDX_RUNNING = IndexDef.create(Commit.class, "running");

    public static BiConsumer<Long, Id> META_CONTEXT_INVALIDATE_HOOK;

    private final Date time = new Date();
    private final WAL wal;
    @ChildEntity
    private final ReadWriteArray<String> newFieldIds = addChild(new ReadWriteArray<>(String.class), "newFieldIds");
    @ChildEntity
    private final ReadWriteArray<String> convertingFieldIds = addChild(new ReadWriteArray<>(String.class), "convertingFieldIds");
    @ChildEntity
    private final ReadWriteArray<String> toChildFieldIds = addChild(new ReadWriteArray<>(String.class), "toChildFieldIds");
    @ChildEntity
    private final ReadWriteArray<String> toNonChildFieldIds = addChild(new ReadWriteArray<>(String.class), "toNonChildFieldIds");
    @ChildEntity
    private final ReadWriteArray<String> removedChildFieldIds = addChild(new ReadWriteArray<>(String.class), "removedChildFieldIds");
    @ChildEntity
    private final ReadWriteArray<String> changingSuperKlassIds = addChild(new ReadWriteArray<>(String.class), "changingSuperKlassIds");
    @ChildEntity
    private final ReadWriteArray<String> entityToValueKlassIds = addChild(new ReadWriteArray<>(String.class), "entityToValueKlassIds");
    @ChildEntity
    private final ReadWriteArray<String> valueToEntityKlassIds = addChild(new ReadWriteArray<>(String.class), "valueToEntityKlassIds");
    @ChildEntity
    private final ReadWriteArray<FieldChange> fieldChanges = addChild(new ReadWriteArray<>(FieldChange.class), "fieldChanges");
    @ChildEntity
    private final ReadWriteArray<String> newEnumConstantIds = addChild(new ReadWriteArray<>(String.class), "newEnumConstantIds");
    @ChildEntity
    private final ReadWriteArray<String> changedEnumConstantIds = addChild(new ReadWriteArray<>(String.class), "changedEnumConstantIds");
    @ChildEntity
    private final ReadWriteArray<String> toEnumKlassIds = addChild(new ReadWriteArray<>(String.class), "toEnumKlassIds");
    @ChildEntity
    private final ReadWriteArray<String> fromEnumKlassIds = addChild(new ReadWriteArray<>(String.class), "fromEnumKlassIds");
    @ChildEntity(since = 2)
    private final ReadWriteArray<String> runMethodIds = addChild(new ReadWriteArray<>(String.class), "runMethodIds");
    @ChildEntity
    private final ReadWriteArray<String> newIndexIds = addChild(new ReadWriteArray<>(String.class), "newIndexIds");
    @ChildEntity
    private final ReadWriteArray<String> searchEnabledKlassIds = addChild(new ReadWriteArray<>(String.class), "searchEnabledKlassIds");

    private CommitState state = CommitState.PREPARING0;
    private boolean running = true;
    private boolean cancelled = false;
    private boolean submitted;

    public Commit(WAL wal,
                  List<String> newFieldIds,
                  List<String> convertingFieldIds,
                  List<String> toChildFieldIds,
                  List<String> toNonChildFieldIds,
                  List<String> removedChildFieldIds,
                  List<String> changingSuperKlassIds,
                  List<String> entityToValueKlassIds,
                  List<String> valueToEntityKlassIds,
                  List<String> toEnumKlassIds,
                  List<String> fromEnumKlassIds,
                  List<String> runMethodIds,
                  List<String> newIndexIds,
                  List<String> searchEnabledKlassIds,
                  List<String> changedEnumConstantIds,
                  List<FieldChange> fieldChanges) {
        this.wal = wal;
        this.newFieldIds.addAll(newFieldIds);
        this.convertingFieldIds.addAll(convertingFieldIds);
        this.toChildFieldIds.addAll(toChildFieldIds);
        this.toNonChildFieldIds.addAll(toNonChildFieldIds);
        this.removedChildFieldIds.addAll(removedChildFieldIds);
        this.changingSuperKlassIds.addAll(changingSuperKlassIds);
        this.entityToValueKlassIds.addAll(entityToValueKlassIds);
        this.valueToEntityKlassIds.addAll(valueToEntityKlassIds);
        this.toEnumKlassIds.addAll(toEnumKlassIds);
        this.fromEnumKlassIds.addAll(fromEnumKlassIds);
        this.runMethodIds.addAll(runMethodIds);
        this.newIndexIds.addAll(newIndexIds);
        this.searchEnabledKlassIds.addAll(searchEnabledKlassIds);
        this.changedEnumConstantIds.addAll(changedEnumConstantIds);
        this.fieldChanges.addAll(fieldChanges);
    }

    public void submit() {
        if(submitted)
            throw new IllegalStateException("Commit is already submitted");
        this.submitted = true;
        wal.commit();
        if(META_CONTEXT_INVALIDATE_HOOK != null) {
            META_CONTEXT_INVALIDATE_HOOK.accept(wal.getAppId(), null);
            META_CONTEXT_INVALIDATE_HOOK.accept(wal.getAppId(), wal.getId());
        }
    }

    public boolean hasCleanUpWorks() {
        return !valueToEntityKlassIds.isEmpty();
    }

    public void finish() {
        if(state == CommitState.COMPLETED)
            throw new IllegalStateException("Commit is already finished");
        this.state = CommitState.COMPLETED;
    }

    public void setState(CommitState state) {
        if(state.ordinal() <= this.state.ordinal())
            throw new IllegalStateException("Invalid state transition from " + this.state + " to " + state);
        this.state = state;
        if(state.isTerminal())
            running = false;
    }

    public Date getTime() {
        return time;
    }

    public CommitState getState() {
        return state;
    }

    public List<String> getNewFieldIds() {
        return newFieldIds.toList();
    }

    public List<String> getConvertingFieldIds() {
        return convertingFieldIds.toList();
    }

    public List<String> getToNonChildFieldIds() {
        return toNonChildFieldIds.toList();
    }

    public List<String> getRemovedChildFieldIds() {
        return removedChildFieldIds.toList();
    }

    public List<String> getToChildFieldIds() {
        return toChildFieldIds.toList();
    }

    public List<String> getChangingSuperKlassIds() {
        return changingSuperKlassIds.toList();
    }

    public List<String> getEntityToValueKlassIds() {
        return entityToValueKlassIds.toList();
    }

    public List<String> getValueToEntityKlassIds() {
        return valueToEntityKlassIds.toList();
    }

    public List<FieldChange> getFieldChanges() {
        return fieldChanges.toList();
    }

    public List<String> getNewEnumConstantIds() {
        return newEnumConstantIds.toList();
    }

    public List<String> getChangedEnumConstantIds() {
        return changedEnumConstantIds.toList();
    }

    public List<String> getToEnumKlassIds() {
        return toEnumKlassIds.toList();
    }

    public List<String> getFromEnumKlassIds() {
        return fromEnumKlassIds.toList();
    }

    public List<String> getRunMethodIds() {
        return runMethodIds.toList();
    }

    public List<String> getNewIndexIds() {
        return newIndexIds.toList();
    }

    public List<String> getSearchEnabledKlassIds() {
        return searchEnabledKlassIds.toList();
    }

    public void cancel() {
        if(cancelled)
            throw new IllegalStateException("The commit has already been cancelled");
        if(!state.isPreparing())
            throw new IllegalStateException("Cannot cancel a prepared commit");
        cancelled = true;
    }

    public WAL getWal() {
        return wal;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean shouldRedirect() {
        return state != CommitState.PREPARING0 && state != CommitState.ABORTING && state != CommitState.ABORTED;
    }
}
