package org.manul.context;

import lombok.Getter;

import javax.lang.model.element.Element;

@Getter
public class ContextConfigException extends RuntimeException {

    private final Element element;

    public ContextConfigException(String message, Element element) {
        super(message);
        this.element = element;
    }

}
