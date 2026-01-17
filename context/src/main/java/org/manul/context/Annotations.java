package org.manul.context;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import javax.naming.directory.AttributeModificationException;
import java.util.Map;

class Annotations {

    static AnnotationMirror getAnnotation(Element element, String fqn) {
        var at = findAnnotation(element, fqn);
        if (at == null)
            throw new IllegalArgumentException("Annotation " + fqn + " not found on element " + element.getSimpleName());
        return at;
    }

    @Nullable
    static AnnotationMirror findAnnotation(Element element, String fqn) {
        for (AnnotationMirror at : element.getAnnotationMirrors()) {
            var cls = (TypeElement) at.getAnnotationType().asElement();
            if (cls.getQualifiedName().contentEquals(fqn))
                return at;
        }
        return null;
    }

    static Object getAttribute(AnnotationMirror at, String name, Object defaultValue) {
        for (var e : at.getElementValues().entrySet()) {
            if (e.getKey().getSimpleName().contentEquals(name))
                return e.getValue().getValue();
        }
        return defaultValue;
    }

}
