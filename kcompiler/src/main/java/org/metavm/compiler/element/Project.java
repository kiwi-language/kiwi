package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.syntax.*;

import java.util.function.Consumer;

@Slf4j
public class Project implements Element {

    private final Package rootPackage = new Package(SymNameTable.instance.empty, null, this);

    public Package getRootPackage() {
        return rootPackage;
    }


    public Clazz classForName(String name) {
        var splits = name.split("\\.");
        var pkg = rootPackage;
        for (int i = 0; i < splits.length - 1; i++) {
            pkg = pkg.subPackage(splits[i]);
        }
        return pkg.getClass(SymNameTable.instance.get(splits[splits.length - 1]));
    }

    public Package getOrCreatePackage(Name qualifiedName) {
        if (qualifiedName instanceof Ident name)
            return rootPackage.subPackage(name.value());
        else if (qualifiedName instanceof QualifiedName selectorExpr)
            return getOrCreatePackage(selectorExpr.getQualifier()).subPackage(selectorExpr.getIdent().value());
        else
            throw new ParsingException("Invalid package name " + qualifiedName.getText());
    }

    public Package getPackage(Name qualifiedName) {
        if (qualifiedName instanceof Ident name)
            return rootPackage.getPackage(name.value());
        else if (qualifiedName instanceof QualifiedName selectorExpr)
            return getPackage(selectorExpr.getQualifier()).getPackage(selectorExpr.getIdent().value());
        else
            throw new ParsingException("Invalid package name: " + qualifiedName.getText());
    }

    @Override
    public SymName getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitProject(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {
        action.accept(rootPackage);
    }

    @Override
    public void write(ElementWriter writer) {
        throw new UnsupportedOperationException();
    }

    public void traceClasses() {
        rootPackage.forEachPackage(pkg -> {
            log.trace("Package: {}", pkg.getQualifiedName());
            for (Clazz aClass : pkg.getClasses()) {
                log.trace("Class: {}", aClass.getName());
            }
        });

    }

}
