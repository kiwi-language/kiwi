package org.metavm.object.instance.search;

import org.metavm.common.Page;
import org.metavm.object.instance.core.Id;
import org.metavm.util.SearchSyncRequest;

public interface InstanceSearchService {

    void createIndex(long appId, boolean tmp);

    void createIndexIfNotExists(long appId);

    void switchAlias(long appId);

    void deleteAllIndices(long appId);

    void revert(long appId);

    Page<Id> search(SearchQuery query);

    long count(SearchQuery query);

    void bulk(SearchSyncRequest request);
}
