package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Element;
import org.metavm.compiler.util.List;


public abstract class ModifiedDecl<E extends Element> extends Decl<E> {

    public abstract List<Modifier> getMods();

}
