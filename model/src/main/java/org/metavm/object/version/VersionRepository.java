package org.metavm.object.version;

import org.metavm.object.instance.core.Id;

import javax.annotation.Nullable;

public interface VersionRepository {

    Id allocateId();

    @Nullable Version getLastVersion();

    void save(Version version);

}
