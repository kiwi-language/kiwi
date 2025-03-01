package org.metavm.compiler.analyze;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.Element;
import org.metavm.compiler.element.ElementTable;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.io.Closeable;

@Slf4j
public class Scope implements Closeable {

    private final Env env;
    private final @Nullable Scope parent;
    private List<Object> entered = List.nil();
//    private final MultiMap<String, Element> map;
    private final ElementTable table;

    public Scope(ElementTable table, Env env, @Nullable Scope parent) {
        this.table = table;
        this.env = env;
        this.parent = parent;
    }

    void add(Element element) {
        entered = entered.prepend(element);
        table.add(element);
    }

    void addAll(ElementTable table) {
        entered = entered.prepend(table);
        this.table.addAll(table);
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
