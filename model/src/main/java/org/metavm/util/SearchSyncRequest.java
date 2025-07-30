package org.metavm.util;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;

import java.util.List;

public record SearchSyncRequest(
        long appId,
        boolean migrating,
        List<ClassInstance> changedInstances,
        List<Id> removedInstanceIds,
        boolean waitUtilRefresh
) {
}
