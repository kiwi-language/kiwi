package org.metavm.wire.processor;

import com.sun.source.util.Trees;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class MyMessenger {
    private final Trees trees;

    public MyMessenger(Trees trees) {
        this.trees = trees;
    }

    void error(String message, Element element) {
        trees.printMessage(
                Diagnostic.Kind.ERROR,
                message,
                trees.getTree(element),
                trees.getPath(element).getCompilationUnit()
        );
    }

}
