package org.metavm.ddl;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IndexDef;
import org.metavm.object.type.rest.dto.BatchSaveRequest;
import org.metavm.util.NncUtils;

import java.util.Date;

@EntityType
public class Commit extends Entity {

    public static final IndexDef<Commit> IDX_STATE = IndexDef.create(Commit.class, "state");

    private final String requestJSON;
    private final Date time = new Date();
    private CommitState state = CommitState.RUNNING;

    private transient BatchSaveRequest request;

    public Commit(BatchSaveRequest request) {
        this.requestJSON = NncUtils.toJSONString(request);
        this.request = request;
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
}
