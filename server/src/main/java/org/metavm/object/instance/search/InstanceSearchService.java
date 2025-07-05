package org.metavm.object.instance.search;

import org.metavm.common.Page;
import org.metavm.object.instance.core.Id;
import org.metavm.util.SearchSyncRequest;

public interface InstanceSearchService {

    Page<Id> search(SearchQuery query);

    long count(SearchQuery query);

    void bulk(SearchSyncRequest request);
}
