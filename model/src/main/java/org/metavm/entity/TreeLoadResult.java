package org.metavm.entity;

import java.util.List;

public record TreeLoadResult(
        List<Tree> trees
) {

    public static TreeLoadResult of(Tree tree) {
        return new TreeLoadResult(List.of(tree));
    }

}
