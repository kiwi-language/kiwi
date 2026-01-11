package org.manul.task;

import org.manul.ddl.Commit;
import org.manul.ddl.CommitState;

public interface IDDLTask {

    Commit getCommit();

    CommitState getCommitState();

}
