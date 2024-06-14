package org.metavm.object.instance.search;

import org.metavm.common.Page;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;

import java.util.List;

public interface InstanceSearchService {

    Page<Id> search(SearchQuery query);

    long count(SearchQuery query);

    void bulk(long appId, List<ClassInstance> toIndex, List<Id> toDelete);
}
