package org.metavm.object.version;

import javax.annotation.Nullable;

public interface VersionRepository {

    @Nullable Version getLastVersion();

    void save(Version version);

}
