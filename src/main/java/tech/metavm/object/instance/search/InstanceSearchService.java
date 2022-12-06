package tech.metavm.object.instance.search;

import tech.metavm.dto.Page;
import tech.metavm.object.instance.Instance;

import java.util.List;

public interface InstanceSearchService {
    Page<Long> search(SearchQuery query);

    void bulk(long tenantId, List<Instance> toIndex, List<Long> toDelete);
}
