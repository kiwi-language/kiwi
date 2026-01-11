package org.manul.compiler.analyze;

import org.manul.compiler.element.Element;

import java.util.function.Function;

abstract class Resolver {

    abstract Element resolve();

    abstract Element resolve(Function<Element, Element> mapper);

}
