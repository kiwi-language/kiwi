package org.metavm.entity;

import org.metavm.util.ForwardingPointer;

import java.util.List;

public record TreeLoadResult(
        List<Tree> trees,
        boolean migrated,
        List<ForwardingPointer> forwardingPointers
) {

    public static TreeLoadResult of(Tree tree) {
        return new TreeLoadResult(List.of(tree), false, List.of());
    }

}
