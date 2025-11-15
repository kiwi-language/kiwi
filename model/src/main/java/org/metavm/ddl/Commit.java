package org.metavm.ddl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Message;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.RedirectStatus;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Wire(13)
@Entity
@Slf4j
public class Commit extends org.metavm.entity.Entity implements RedirectStatus, Message {

    public static final IndexDef<Commit> IDX_RUNNING = IndexDef.create(Commit.class,
            1, commit -> List.of(Instances.booleanInstance(commit.running)));

    public static BiConsumer<Long, Boolean> META_CONTEXT_INVALIDATE_HOOK;
    public static BiConsumer<Long, Id> tableSwitchHook;
    public static BiConsumer<Long, Id> dropTmpTableHook;
    public static Consumer<Long> cleanupRemovingClassesHook;

    private final Date time = new Date();
    private final long appId;
    private final List<String> newFieldIds = new ArrayList<>();
    private final List<String> convertingFieldIds = new ArrayList<>();
    private final List<String> toChildFieldIds = new ArrayList<>();
    private final List<String> toNonChildFieldIds = new ArrayList<>();
    private final List<String> removedChildFieldIds = new ArrayList<>();
    private final List<String> changingSuperKlassIds = new ArrayList<>();
    private final List<String> entityToValueKlassIds = new ArrayList<>();
    private final List<String> valueToEntityKlassIds = new ArrayList<>();
    private final List<FieldChange> fieldChanges = new ArrayList<>();
    private final List<String> newEnumConstantIds = new ArrayList<>();
    private final List<String> changedEnumConstantIds = new ArrayList<>();
    private final List<String> toEnumKlassIds = new ArrayList<>();
    private final List<String> fromEnumKlassIds = new ArrayList<>();
    private final List<String> runMethodIds = new ArrayList<>();
    private final List<String> newIndexIds = new ArrayList<>();
    private final List<String> searchEnabledKlassIds = new ArrayList<>();

    private CommitState state = CommitState.MIGRATING;
    private boolean running = true;
    private boolean cancelled = false;
    private final boolean noBackup;
    private boolean submitted;

    public Commit(@NotNull Id id,
                  long appId,
                  boolean noBackup,
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
        super(id);
        this.appId = appId;
        this.noBackup = noBackup;
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
        tableSwitchHook.accept(appId, getId());
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
            throw new IllegalStateException("Invalid state transition from " + this.state + " to " + state + ". Commit ID: " + getId());
        this.state = state;
    }

    public void terminate() {
        this.running = false;
        if(META_CONTEXT_INVALIDATE_HOOK != null) {
            META_CONTEXT_INVALIDATE_HOOK.accept(appId, false);
            META_CONTEXT_INVALIDATE_HOOK.accept(appId, true);
        }
    }

    public Date getTime() {
        return time;
    }

    public CommitState getState() {
        return state;
    }

    public List<String> getNewFieldIds() {
        return Collections.unmodifiableList(newFieldIds);
    }

    public List<String> getConvertingFieldIds() {
        return Collections.unmodifiableList(convertingFieldIds);
    }

    public List<String> getToNonChildFieldIds() {
        return Collections.unmodifiableList(toNonChildFieldIds);
    }

    public List<String> getRemovedChildFieldIds() {
        return Collections.unmodifiableList(removedChildFieldIds);
    }

    public List<String> getToChildFieldIds() {
        return Collections.unmodifiableList(toChildFieldIds);
    }

    public List<String> getChangingSuperKlassIds() {
        return Collections.unmodifiableList(changingSuperKlassIds);
    }

    public List<String> getEntityToValueKlassIds() {
        return Collections.unmodifiableList(entityToValueKlassIds);
    }

    public List<String> getValueToEntityKlassIds() {
        return Collections.unmodifiableList(valueToEntityKlassIds);
    }

    public List<FieldChange> getFieldChanges() {
        return Collections.unmodifiableList(fieldChanges);
    }

    public List<String> getNewEnumConstantIds() {
        return Collections.unmodifiableList(newEnumConstantIds);
    }

    public List<String> getChangedEnumConstantIds() {
        return Collections.unmodifiableList(changedEnumConstantIds);
    }

    public List<String> getToEnumKlassIds() {
        return Collections.unmodifiableList(toEnumKlassIds);
    }

    public List<String> getFromEnumKlassIds() {
        return Collections.unmodifiableList(fromEnumKlassIds);
    }

    public List<String> getRunMethodIds() {
        return Collections.unmodifiableList(runMethodIds);
    }

    public List<String> getNewIndexIds() {
        return Collections.unmodifiableList(newIndexIds);
    }

    public List<String> getSearchEnabledKlassIds() {
        return Collections.unmodifiableList(searchEnabledKlassIds);
    }

    public void cancel() {
        if(cancelled)
            throw new IllegalStateException("The commit has already been cancelled");
        if(!state.isPreparing())
            throw new IllegalStateException("Cannot cancel a prepared commit");
        cancelled = true;
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

    public boolean isNoBackup() {
        return noBackup;
    }

    @Override
    public boolean shouldRedirect() {
        return state != CommitState.MIGRATING && state != CommitState.ABORTING && state != CommitState.ABORTED;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    private void writeIds(MvOutput output, List<String> ids) {
        output.writeInt(ids.size());
        ids.forEach(output::writeUTF);
    }

    private static List<String> readIds(MvInput input) {
        var cnt = input.readInt();
        var ids = new ArrayList<String>(cnt);
        for (int i = 0; i < cnt; i++) {
            ids.add(input.readUTF());
        }
        return ids;
    }

    private static List<FieldChange> readFieldChanges(MvInput input) {
        int cnt = input.readInt();
        var fieldChanges = new ArrayList<FieldChange>();
        for (int i = 0; i < cnt; i++) {
            fieldChanges.add(FieldChange.read(input));
        }
        return fieldChanges;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        for (var fieldChanges_ : fieldChanges) fieldChanges_.forEachReference(action);
    }

    public long getAppId() {
        return appId;
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
