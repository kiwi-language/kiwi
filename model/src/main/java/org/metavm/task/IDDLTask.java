package org.metavm.task;

import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;

public interface IDDLTask {

    Commit getCommit();

    CommitState getCommitState();

}
