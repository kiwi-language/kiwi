package org.manul.object.instance;

import org.manul.entity.VersionSource;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.TreeVersion;

import java.util.List;

public class StoreVersionSource implements VersionSource {

    private final IInstanceStore instanceStore;

    public StoreVersionSource(IInstanceStore instanceStore) {
        this.instanceStore = instanceStore;
    }

    @Override
    public List<TreeVersion> getVersions(List<Long> ids, IInstanceContext context) {
        return instanceStore.getVersions(ids, context);
    }
}
