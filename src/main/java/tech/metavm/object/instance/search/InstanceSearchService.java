package tech.metavm.object.instance.search;

import tech.metavm.common.Page;
import tech.metavm.object.instance.core.ClassInstance;

import java.util.List;

public interface InstanceSearchService {

    Page<Long> search(SearchQuery query);

    void bulk(long tenantId, List<ClassInstance> toIndex, List<Long> toDelete);
}
