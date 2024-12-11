package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.Element;

@Entity
public class Error extends org.metavm.entity.Entity {

    private final Element element;

    private final ErrorLevel level;

    private final String message;

    public Error(Element element, ErrorLevel level, String message) {
        this.element = element;
        this.level = level;
        this.message = message;
    }

    public Element getElement() {
        return element;
    }

    public ErrorLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

}
