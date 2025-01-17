package org.metavm.entity;

import org.metavm.util.Utils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public record StoreLoadRequestItem(
        long id,
        Set<LoadingOption> options
) {

    public boolean isOptionPresent(LoadingOption option) {
        return options.contains(option);
    }

    public boolean isOptionAbsent(LoadingOption option) {
        return !isOptionPresent(option);
    }

    public static StoreLoadRequestItem of(long id) {
        return new StoreLoadRequestItem(id, Set.of());
    }

    public static StoreLoadRequestItem of(long id, Set<LoadingOption> options) {
        return new StoreLoadRequestItem(id, options);
    }

    public static List<StoreLoadRequestItem> of(Collection<Long> ids, Set<LoadingOption> options) {
        return Utils.map(ids, id -> new StoreLoadRequestItem(id, options));
    }

}
