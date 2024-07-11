package org.metavm.entity;

import org.metavm.util.ForwardingPointer;

import java.util.List;

public record TreeLoadResult(
        Tree tree,
        boolean migrated,
        List<ForwardingPointer> forwardingPointers
) {

    public static TreeLoadResult of(Tree tree) {
        return new TreeLoadResult(tree, false, List.of());
    }

    public static TreeLoadResult ofMigrated(Tree tree, List<ForwardingPointer> pointers) {
        return new TreeLoadResult(tree, true, pointers);
    }

}
