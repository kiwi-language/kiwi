package org.manul.context;

import lombok.Getter;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

@Getter
public class ContextConfigException extends RuntimeException {

    private final Element element;
    private final AnnotationMirror annotation;

    public ContextConfigException(String message, Element element) {
        this(message, element, null);
    }

    public ContextConfigException(String message, Element element, @Nullable AnnotationMirror annotation) {
        super(message);
        this.element = element;
        this.annotation = annotation;
    }

}
