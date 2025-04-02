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

    public Scope(Node node, ElementTable table, Env env, @Nullable Scope parent) {
        this.node = node;
        this.table = table;
        this.env = env;
        this.parent = parent;
    }

    public Node getNode() {
        return node;
    }

    public Element getElement() {
        return node instanceof Decl<?> decl ? decl.getElement() : (
                node instanceof LambdaExpr l ? l.getElement() : null
        );
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
}
