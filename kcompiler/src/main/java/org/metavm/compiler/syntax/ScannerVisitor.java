package org.metavm.compiler.syntax;

import org.metavm.compiler.util.List;

import java.util.Objects;
import java.util.function.Predicate;

public class ScannerVisitor extends StructuralNodeVisitor {
    private List<Node> nodes = List.nil();

    @Override
    public Void visitNode(Node node) {
        nodes = nodes.prepend(node);
        return super.visitNode(node);
    }

    protected <T extends Node> T getAncestor(Class<T> clazz) {
        //noinspection unchecked
        return (T) Objects.requireNonNull(nodes.find(clazz::isInstance),
                () -> "Cannot find ancestor of class " + clazz.getName());
    }

    protected Node findAncestor(Predicate<? super Node> filter) {
        return nodes.find(filter);
    }

}
