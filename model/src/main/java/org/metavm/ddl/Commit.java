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
    private final ReadWriteArray<String> toValueKlassIds = addChild(new ReadWriteArray<>(String.class), "toValueKlassIds");

    private CommitState state = CommitState.RUNNING;

    private transient BatchSaveRequest request;

    public Commit(WAL wal,
                  BatchSaveRequest request,
                  List<String> newFieldIds,
                  List<String> convertingFieldIds,
                  List<String> toChildFieldIds,
                  List<String> changingSuperKlassIds,
                  List<String> toValueKlassIds) {
        this.wal = wal;
        this.requestJSON = NncUtils.toJSONString(request);
        this.request = request;
        this.newFieldIds.addAll(newFieldIds);
        this.convertingFieldIds.addAll(convertingFieldIds);
        this.toChildFieldIds.addAll(toChildFieldIds);
        this.changingSuperKlassIds.addAll(changingSuperKlassIds);
        this.toValueKlassIds.addAll(toValueKlassIds);
    }

    public BatchSaveRequest getRequest() {
        if (request == null)
            request = NncUtils.readJSONString(requestJSON, BatchSaveRequest.class);
        return request;
    }

    public void finish() {
        if (state == CommitState.FINISHED)
            throw new IllegalStateException("Commit already finished");
        this.state = CommitState.FINISHED;
        wal.commit();
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

    public List<String> getToValueKlassIds() {
        return toValueKlassIds.toList();
    }

    public WAL getWal() {
        return wal;
    }

}
