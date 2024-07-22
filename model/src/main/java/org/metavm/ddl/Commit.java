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

    public static final IndexDef<Commit> IDX_STATE = IndexDef.create(Commit.class, "state");

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
    private final ReadWriteArray<String> changingSuperKlassIds = addChild(new ReadWriteArray<>(String.class), "changingSuperKlassIds");
    @ChildEntity
    private final ReadWriteArray<String> entityToValueKlassIds = addChild(new ReadWriteArray<>(String.class), "entityToValueKlassIds");
    @ChildEntity
    private final ReadWriteArray<String> valueToEntityKlassIds = addChild(new ReadWriteArray<>(String.class), "valueToEntityKlassIds");

    private CommitState state = CommitState.RUNNING;

    private transient BatchSaveRequest request;

    public Commit(WAL wal,
                  BatchSaveRequest request,
                  List<String> newFieldIds,
                  List<String> convertingFieldIds,
                  List<String> toChildFieldIds,
                  List<String> changingSuperKlassIds,
                  List<String> entityToValueKlassIds,
                  List<String> valueToEntityKlassIds) {
        this.wal = wal;
        this.requestJSON = NncUtils.toJSONString(request);
        this.request = request;
        this.newFieldIds.addAll(newFieldIds);
        this.convertingFieldIds.addAll(convertingFieldIds);
        this.toChildFieldIds.addAll(toChildFieldIds);
        this.changingSuperKlassIds.addAll(changingSuperKlassIds);
        this.entityToValueKlassIds.addAll(entityToValueKlassIds);
        this.valueToEntityKlassIds.addAll(valueToEntityKlassIds);
    }

    public BatchSaveRequest getRequest() {
        if (request == null)
            request = NncUtils.readJSONString(requestJSON, BatchSaveRequest.class);
        return request;
    }

    public void submit() {
        if (state != CommitState.RUNNING)
            throw new IllegalStateException("Commit is already submitted");
        this.state = hasCleanUpWorks() ? CommitState.CLEANING_UP : CommitState.FINISHED;
        wal.commit();
    }

    public boolean hasCleanUpWorks() {
        return !valueToEntityKlassIds.isEmpty();
    }

    public void finish() {
        if(state == CommitState.FINISHED)
            throw new IllegalStateException("Commit is already finished");
        this.state = CommitState.FINISHED;
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

    public WAL getWal() {
        return wal;
    }

}
