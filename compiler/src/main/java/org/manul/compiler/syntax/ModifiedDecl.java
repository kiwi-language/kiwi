package org.manul.compiler.syntax;

import org.manul.compiler.element.Element;
import org.manul.compiler.util.List;


public abstract class ModifiedDecl<E extends Element> extends Decl<E> {

    public abstract List<Modifier> getMods();

}
