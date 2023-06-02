package tech.metavm.transpile.ir;

import java.lang.annotation.Annotation;

public record IRAnnotation(
        Class<? extends Annotation> klass
) {

}
