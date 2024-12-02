package org.metavm.object.type;

import org.metavm.entity.Element;
import org.metavm.entity.Writable;
import org.metavm.flow.KlassOutput;

public class ElementCpEntry extends CpEntry {

    private final Element element;

    public ElementCpEntry(int index, Element element) {
        super(index);
        this.element = element;
    }

    @Override
    public Element getValue() {
        return element;
    }

    @Override
    public void write(KlassOutput output) {
        ((Writable) element).write(output);
    }
}
