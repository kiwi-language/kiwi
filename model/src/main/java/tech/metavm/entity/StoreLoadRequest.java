package tech.metavm.entity;

import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Predicate;

public class StoreLoadRequest {

    public static StoreLoadRequest fromLoadRequests(List<LoadRequest> loadRequests) {
        if (NncUtils.isEmpty(loadRequests)) {
            return new StoreLoadRequest(List.of());
        }
        Class<?> entityType = null;
        List<Long> ids = new ArrayList<>();
        for (LoadRequest loadRequest : loadRequests) {
            ids.add(loadRequest.id());
        }
        return create(ids);
    }

    public static StoreLoadRequest create(Long id) {
        return create(List.of(id));
    }

    public static StoreLoadRequest create(List<Long> ids) {
        return new StoreLoadRequest(
                NncUtils.map(ids, id -> new StoreLoadRequestItem(id, LoadingOption.none()))
        );
    }

    public static StoreLoadRequest create(List<Long> ids, Set<LoadingOption> options) {
        return new StoreLoadRequest(
                NncUtils.map(ids, id -> new StoreLoadRequestItem(id, options))
        );
    }

    private final Map<Long, StoreLoadRequestItem> itemMap = new LinkedHashMap<>();

    public StoreLoadRequest(List<StoreLoadRequestItem> items) {
        for (StoreLoadRequestItem item : items) {
            itemMap.put(item.id(), item);
        }
    }

    private Collection<StoreLoadRequestItem> items() {
        return itemMap.values();
    }

    public List<Long> ids() {
        return NncUtils.map(items(), StoreLoadRequestItem::id);
    }

    public List<Long> idsWithoutOption(LoadingOption option) {
        return idsWithFilter(item -> item.isOptionAbsent(option));
    }

    public List<Long> getIdsWithOption(LoadingOption option) {
        return idsWithFilter(item -> item.isOptionPresent(option));
    }

    public List<Long> idsWithFilter(Predicate<StoreLoadRequestItem> filter) {
        return NncUtils.filterAndMap(
                items(),
                filter,
                StoreLoadRequestItem::id
        );
    }

}
