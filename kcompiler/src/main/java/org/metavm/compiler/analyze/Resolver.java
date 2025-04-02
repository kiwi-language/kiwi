package org.metavm.compiler.analyze;

import org.metavm.compiler.element.Element;

import java.util.function.Function;

abstract class Resolver {

    abstract Element resolve();

    abstract Element resolve(Function<Element, Element> mapper);

}
