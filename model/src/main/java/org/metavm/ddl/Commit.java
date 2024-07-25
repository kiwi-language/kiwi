package org.metavm.ddl;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IndexDef;
import org.metavm.entity.ReadWriteArray;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.rest.dto.BatchSaveRequest;
import org.metavm.util.NncUtils;

import java.util.Date;
import java.util.List;

@EntityType
public class Commit extends Entity {

    public static final IndexDef<Commit> IDX_RUNNING = IndexDef.create(Commit.class, "running");

    private final String requestJSON;
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

    private CommitState state = CommitState.PREPARING0;
    private boolean running = true;
    private boolean cancelled = false;
    private boolean submitted;

    private transient BatchSaveRequest request;

    public Commit(WAL wal,
                  BatchSaveRequest request,
                  List<String> newFieldIds,
                  List<String> convertingFieldIds,
                  List<String> toChildFieldIds,
                  List<String> toNonChildFieldIds,
                  List<String> changingSuperKlassIds,
                  List<String> entityToValueKlassIds,
                  List<String> valueToEntityKlassIds,
                  List<FieldChange> fieldChanges) {
        this.wal = wal;
        this.requestJSON = NncUtils.toJSONString(request);
        this.request = request;
        this.newFieldIds.addAll(newFieldIds);
        this.convertingFieldIds.addAll(convertingFieldIds);
        this.toChildFieldIds.addAll(toChildFieldIds);
        this.toNonChildFieldIds.addAll(toNonChildFieldIds);
        this.changingSuperKlassIds.addAll(changingSuperKlassIds);
        this.entityToValueKlassIds.addAll(entityToValueKlassIds);
        this.valueToEntityKlassIds.addAll(valueToEntityKlassIds);
        this.fieldChanges.addAll(fieldChanges);
    }

    public BatchSaveRequest getRequest() {
        if (request == null)
            request = NncUtils.readJSONString(requestJSON, BatchSaveRequest.class);
        return request;
    }

    public void submit() {
        if(submitted)
            throw new IllegalStateException("Commit is already submitted");
        this.submitted = true;
        wal.commit();
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
        if(state == CommitState.COMPLETED)
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
}
