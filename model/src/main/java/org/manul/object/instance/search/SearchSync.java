package org.manul.object.instance.search;

import org.manul.common.ErrorCode;
import org.manul.object.instance.core.ClassInstance;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Id;
import org.manul.util.BusinessException;
import org.manul.util.Hooks;
import org.manul.util.SearchSyncRequest;

import java.util.ArrayList;
import java.util.Collection;

public class SearchSync {

    public static void sync(Collection<Id> changedIds, Collection<Id> removedIds, boolean waitUtilRefresh, IInstanceContext context) {
        changedIds.forEach(context::buffer);
        var changed = new ArrayList<ClassInstance>();
        for (Id id : changedIds) {
            try {
                changed.add((ClassInstance) context.get(id));
            }
            catch (BusinessException e) {
                // It's possible that the instance is removed at this moment
                if (e.getErrorCode() != ErrorCode.INSTANCE_NOT_FOUND)
                    throw e;
            }
        }
        try (var ignored = context.getProfiler().enter("bulk")) {
            Hooks.SEARCH_BULK.accept(
                    new SearchSyncRequest(
                            context.getAppId(),
                            context.isMigrating(),
                            changed,
                            new ArrayList<>(removedIds),
                            waitUtilRefresh
                    )
            );
        }
    }

}
