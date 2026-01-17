package org.manul.context;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

public class BeanNotFoundException extends ContextConfigException {
    public BeanNotFoundException(String bean, Element element, @Nullable AnnotationMirror annotation) {
        super("Bean not found: " + bean, element, annotation);
    }
}
