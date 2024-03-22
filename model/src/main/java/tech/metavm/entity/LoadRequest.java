package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public record LoadRequest (
        Id id,
        Set<LoadingOption> options
) {

    public static LoadRequest create(Id id, LoadingOption...options) {
        return new LoadRequest(
                id,
                new HashSet<>(Arrays.asList(options))
        );
    }

}
