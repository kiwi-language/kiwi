package org.manul.object.type;

import org.manul.entity.Element;
import org.manul.entity.ElementVisitor;

public class TypeResolver extends ElementVisitor<Element> {
    public TypeResolver() {
    }

    @Override
    public Element visitElement(Element element) {
        return null;
    }

}
