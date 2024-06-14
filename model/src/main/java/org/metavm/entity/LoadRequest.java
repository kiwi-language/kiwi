package org.metavm.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public record LoadRequest (
        long id,
        Set<LoadingOption> options
) {

    public static LoadRequest create(long id, LoadingOption...options) {
        return new LoadRequest(
                id,
                new HashSet<>(Arrays.asList(options))
        );
    }

}
