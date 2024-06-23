package org.metavm.ddl;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.common.CopyContext;
import org.metavm.entity.Entity;
import org.metavm.entity.IndexDef;
import org.metavm.entity.ReadWriteArray;
import org.metavm.object.type.rest.dto.BatchSaveRequest;
import org.metavm.util.NncUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EntityType
public class Commit extends Entity {

    public static final IndexDef<Commit> IDX_STATE = IndexDef.create(Commit.class, "state");

    private final String requestJSON;
    private final Date time = new Date();
    @ChildEntity
    private final ReadWriteArray<String> newElementTmpIds = addChild(new ReadWriteArray<>(String.class), "newElementTmpIds");
    @ChildEntity
    private final ReadWriteArray<String> newElementIds = addChild(new ReadWriteArray<>(String.class), "newElementIds");
    private CommitState state = CommitState.RUNNING;

    private transient BatchSaveRequest request;
    private transient BatchSaveRequest commitRequest;

    public Commit(BatchSaveRequest request, Map<String, String> elementIdMap) {
        this.requestJSON = NncUtils.toJSONString(request);
        this.request = request;
        elementIdMap.forEach((tmpId, id) -> {
            newElementTmpIds.add(tmpId);
            newElementIds.add(id);
        });
    }

    public BatchSaveRequest getRequest() {
        if(request == null)
            request =  NncUtils.readJSONString(requestJSON, BatchSaveRequest.class);
        return request;
    }

    public void finish() {
        if(state == CommitState.FINISHED)
            throw new IllegalStateException("Commit already finished");
        this.state = CommitState.FINISHED;
    }

    public Date getTime() {
        return time;
    }

    public CommitState getState() {
        return state;
    }

    public List<String> getNewElementIds() {
        return newElementIds;
    }

    public BatchSaveRequest getCommitRequest() {
        if(commitRequest == null) {
            var elementIdMap = new HashMap<String, String>();
            NncUtils.biForEach(newElementTmpIds, newElementIds, elementIdMap::put);
            commitRequest = CopyContext.create(elementIdMap).copy(getRequest());
        }
        return commitRequest;
    }

}
