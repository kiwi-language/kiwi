package org.manul.object.type;

import org.manul.ddl.Commit;

import javax.annotation.Nullable;

public interface ActiveCommitProvider {

    @Nullable Commit getActiveCommit();

}
