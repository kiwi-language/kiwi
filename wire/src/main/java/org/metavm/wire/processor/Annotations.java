package org.metavm.wire.processor;

import com.sun.tools.javac.code.Symbol;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import java.util.Map;

class Annotations {

    static boolean isAnnotationPresent(AnnotatedConstruct construct, Symbol.ClassSymbol clazz) {
        for (AnnotationMirror annotation : construct.getAnnotationMirrors()) {
            var dt = annotation.getAnnotationType();
            if (dt.asElement() == clazz)
                return true;
        }
        return false;
    }

    static AnnotationMirror getAnnotation(AnnotatedConstruct construct, Symbol.ClassSymbol clazz) {
        for (AnnotationMirror annotation : construct.getAnnotationMirrors()) {
            var dt = annotation.getAnnotationType();
            if (dt.asElement() == clazz)
                return annotation;
        }
        return null;
    }

    static Object getAttribute(AnnotatedConstruct construct, Symbol.ClassSymbol clazz, Name attributeName) {
        var annotation = getAnnotation(construct, clazz);
        if (annotation == null)
            throw new IllegalArgumentException("Annotation not present: " + clazz.getSimpleName());
        for (var e : annotation.getElementValues().entrySet()) {
            if (e.getKey().getSimpleName() == attributeName) {
                return e.getValue().getValue();
            }
        }
        return null;
    }

    static Object getAttribute(AnnotationMirror annotation, Name attributeName) {
        for (var e : annotation.getElementValues().entrySet()) {
            if (e.getKey().getSimpleName() == attributeName) {
                return e.getValue().getValue();
            }
        }
        return null;
    }

}
