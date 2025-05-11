package org.metavm.ddl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.RedirectStatus;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

@NativeEntity(13)
@Entity
@Slf4j
public class Commit extends org.metavm.entity.Entity implements RedirectStatus, Message {

    public static final IndexDef<Commit> IDX_RUNNING = IndexDef.create(Commit.class,
            1, commit -> List.of(Instances.booleanInstance(commit.running)));

    public static BiConsumer<Long, Id> META_CONTEXT_INVALIDATE_HOOK;
    public static BiConsumer<Long, Id> tableSwitchHook;
    public static BiConsumer<Long, Id> dropTmpTableHook;
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private Date time = new Date();
    private Reference walReference;
    private List<String> newFieldIds = new ArrayList<>();
    private List<String> convertingFieldIds = new ArrayList<>();
    private List<String> toChildFieldIds = new ArrayList<>();
    private List<String> toNonChildFieldIds = new ArrayList<>();
    private List<String> removedChildFieldIds = new ArrayList<>();
    private List<String> changingSuperKlassIds = new ArrayList<>();
    private List<String> entityToValueKlassIds = new ArrayList<>();
    private List<String> valueToEntityKlassIds = new ArrayList<>();
    private List<FieldChange> fieldChanges = new ArrayList<>();
    private List<String> newEnumConstantIds = new ArrayList<>();
    private List<String> changedEnumConstantIds = new ArrayList<>();
    private List<String> toEnumKlassIds = new ArrayList<>();
    private List<String> fromEnumKlassIds = new ArrayList<>();
    private List<String> runMethodIds = new ArrayList<>();
    private List<String> newIndexIds = new ArrayList<>();
    private List<String> searchEnabledKlassIds = new ArrayList<>();

    private CommitState state = CommitState.MIGRATING;
    private boolean running = true;
    private boolean cancelled = false;
    private boolean submitted;

    public Commit(@NotNull Id id,
                  WAL wal,
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
        this.walReference = wal.getReference();
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

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitLong();
        visitor.visitValue();
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(() -> FieldChange.visit(visitor));
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitByte();
        visitor.visitBoolean();
        visitor.visitBoolean();
        visitor.visitBoolean();
    }

    public void submit() {
        if(submitted)
            throw new IllegalStateException("Commit is already submitted");
        this.submitted = true;
        var wal = getWal();
        wal.commit();
        tableSwitchHook.accept(wal.getAppId(), getId());
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
    }

    public void setRunning(boolean running) {
        this.running = running;
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
        if(!state.isMigrating())
            throw new IllegalStateException("Cannot cancel a prepared commit");
        cancelled = true;
    }

    public WAL getWal() {
        return (WAL) walReference.get();
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
        action.accept(walReference);
        for (var fieldChanges_ : fieldChanges) fieldChanges_.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("time", this.getTime().getTime());
        map.put("state", this.getState().name());
        map.put("newFieldIds", this.getNewFieldIds());
        map.put("convertingFieldIds", this.getConvertingFieldIds());
        map.put("toNonChildFieldIds", this.getToNonChildFieldIds());
        map.put("removedChildFieldIds", this.getRemovedChildFieldIds());
        map.put("toChildFieldIds", this.getToChildFieldIds());
        map.put("changingSuperKlassIds", this.getChangingSuperKlassIds());
        map.put("entityToValueKlassIds", this.getEntityToValueKlassIds());
        map.put("valueToEntityKlassIds", this.getValueToEntityKlassIds());
        map.put("fieldChanges", this.getFieldChanges().stream().map(FieldChange::toJson).toList());
        map.put("newEnumConstantIds", this.getNewEnumConstantIds());
        map.put("changedEnumConstantIds", this.getChangedEnumConstantIds());
        map.put("toEnumKlassIds", this.getToEnumKlassIds());
        map.put("fromEnumKlassIds", this.getFromEnumKlassIds());
        map.put("runMethodIds", this.getRunMethodIds());
        map.put("newIndexIds", this.getNewIndexIds());
        map.put("searchEnabledKlassIds", this.getSearchEnabledKlassIds());
        map.put("wal", this.getWal().getStringId());
        map.put("running", this.isRunning());
        map.put("submitted", this.isSubmitted());
        map.put("cancelled", this.isCancelled());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_Commit;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.time = input.readDate();
        this.walReference = (Reference) input.readValue();
        this.newFieldIds = input.readList(input::readUTF);
        this.convertingFieldIds = input.readList(input::readUTF);
        this.toChildFieldIds = input.readList(input::readUTF);
        this.toNonChildFieldIds = input.readList(input::readUTF);
        this.removedChildFieldIds = input.readList(input::readUTF);
        this.changingSuperKlassIds = input.readList(input::readUTF);
        this.entityToValueKlassIds = input.readList(input::readUTF);
        this.valueToEntityKlassIds = input.readList(input::readUTF);
        this.fieldChanges = input.readList(() -> FieldChange.read(input));
        this.newEnumConstantIds = input.readList(input::readUTF);
        this.changedEnumConstantIds = input.readList(input::readUTF);
        this.toEnumKlassIds = input.readList(input::readUTF);
        this.fromEnumKlassIds = input.readList(input::readUTF);
        this.runMethodIds = input.readList(input::readUTF);
        this.newIndexIds = input.readList(input::readUTF);
        this.searchEnabledKlassIds = input.readList(input::readUTF);
        this.state = CommitState.fromCode(input.read());
        this.running = input.readBoolean();
        this.cancelled = input.readBoolean();
        this.submitted = input.readBoolean();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeDate(time);
        output.writeValue(walReference);
        output.writeList(newFieldIds, output::writeUTF);
        output.writeList(convertingFieldIds, output::writeUTF);
        output.writeList(toChildFieldIds, output::writeUTF);
        output.writeList(toNonChildFieldIds, output::writeUTF);
        output.writeList(removedChildFieldIds, output::writeUTF);
        output.writeList(changingSuperKlassIds, output::writeUTF);
        output.writeList(entityToValueKlassIds, output::writeUTF);
        output.writeList(valueToEntityKlassIds, output::writeUTF);
        output.writeList(fieldChanges, arg0 -> arg0.write(output));
        output.writeList(newEnumConstantIds, output::writeUTF);
        output.writeList(changedEnumConstantIds, output::writeUTF);
        output.writeList(toEnumKlassIds, output::writeUTF);
        output.writeList(fromEnumKlassIds, output::writeUTF);
        output.writeList(runMethodIds, output::writeUTF);
        output.writeList(newIndexIds, output::writeUTF);
        output.writeList(searchEnabledKlassIds, output::writeUTF);
        output.write(state.code());
        output.writeBoolean(running);
        output.writeBoolean(cancelled);
        output.writeBoolean(submitted);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
    }

}
