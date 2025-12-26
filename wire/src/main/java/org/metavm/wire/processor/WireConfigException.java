package org.metavm.wire.processor;

import lombok.Getter;

import javax.lang.model.element.Element;

@Getter
public class WireConfigException extends RuntimeException {

    private final Element element;

    public WireConfigException(String message, Element element) {
        super(message);
        this.element = element;
    }

}
