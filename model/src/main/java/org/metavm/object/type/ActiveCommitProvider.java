package org.metavm.object.type;

import org.metavm.ddl.Commit;

import javax.annotation.Nullable;

public interface ActiveCommitProvider {

    @Nullable Commit getActiveCommit();

}
