package org.manul.object.instance.search;

import org.manul.common.Page;
import org.manul.object.instance.core.Id;
import org.manul.util.SearchSyncRequest;

public interface InstanceSearchService {

    void createIndex(long appId, boolean tmp);

    void createIndexIfNotExists(long appId);

    void switchAlias(long appId, boolean backup);

    void deleteAllIndices(long appId);

    void deleteTmpIndex(long appId);

    void revert(long appId);

    Page<Id> search(SearchQuery query);

    long count(SearchQuery query);

    void bulk(SearchSyncRequest request);
}
