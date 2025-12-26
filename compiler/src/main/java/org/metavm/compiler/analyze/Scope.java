package org.metavm.compiler.analyze;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.Element;
import org.metavm.compiler.element.ElementTable;
import org.metavm.compiler.syntax.Decl;
import org.metavm.compiler.syntax.LambdaExpr;
import org.metavm.compiler.syntax.Node;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.io.Closeable;

@Slf4j
public class Scope implements Closeable {

    private final Node node;
    private final Env env;
    private final @Nullable Scope parent;
    private List<Object> entered = List.nil();
    private final ElementTable table;
    private final Element element;

    public Scope(@Nullable Node node, @Nullable Element element, ElementTable table, Env env, @Nullable Scope parent) {
        this.node = node;
        this.table = table;
        this.env = env;
        this.parent = parent;
        this.element = element != null ? element : (
                switch (node) {
                    case Decl<?> decl -> decl.getElement();
                    case LambdaExpr lambdaExpr -> lambdaExpr.getElement();
                    case null, default -> null;
                }
        );
    }

    public @Nullable Node getNode() {
        return node;
    }

    public @Nullable Element getElement() {
        return element;
    }

    void add(Element element) {
        entered = entered.prepend(element);
        table.add(element);
    }

    void addAll(ElementTable table) {
        entered = entered.prepend(table);
        this.table.addAll(table);
    }

    @Nullable
    public Scope getParent() {
        return parent;
    }

    @Override
    public void close() {
        entered.forEach(e -> {
            if (e instanceof Element ele)
                table.remove(ele);
            else if (e instanceof ElementTable tbl)
                table.removeAll(tbl);
            else
                throw new RuntimeException("Unrecognized entered element: " + e);
        });
        env.setCurrentScope(parent);
    }

    @Override
    public String toString() {
        return table.toString();
    }
}
