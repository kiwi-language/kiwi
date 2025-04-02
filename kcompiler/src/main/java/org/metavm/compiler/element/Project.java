package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.syntax.*;

import java.util.function.Consumer;

@Slf4j
public class Project extends ElementBase implements Element {

    private final Package rootPackage = new Package(NameTable.instance.empty, null, this);

    public Package getRootPackage() {
        return rootPackage;
    }

    public Package getPackage(String qualname) {
        var splits = qualname.split("\\.");
        var pkg = rootPackage;
        for (String split : splits) {
            pkg = pkg.subPackage(split);
        }
        return pkg;
    }

    public Clazz getClass(String qualName) {
        var splits = qualName.split("\\.");
        var pkg = getRootPackage();
        for (int i = 0; i < splits.length - 1; i++) {
            pkg = pkg.subPackage(splits[i]);
        }
        return pkg.getClass(splits[splits.length - 1]);
    }

    public Clazz classForName(String name) {
        var splits = name.split("\\.");
        var pkg = rootPackage;
        for (int i = 0; i < splits.length - 1; i++) {
            pkg = pkg.subPackage(splits[i]);
        }
        return pkg.getClass(NameTable.instance.get(splits[splits.length - 1]));
    }

    public Package getOrCreatePackage(Expr qualifiedName) {
        if (qualifiedName instanceof Ident name)
            return rootPackage.subPackage(name.getName());
        else if (qualifiedName instanceof SelectorExpr selectorExpr)
            return getOrCreatePackage(selectorExpr.x()).subPackage(selectorExpr.sel());
        else
            throw new ParsingException("Invalid package name " + qualifiedName.getText());
    }

    public Package getPackage(Expr qualifiedName) {
        if (qualifiedName instanceof Ident name)
            return rootPackage.getPackage(name.getName());
        else if (qualifiedName instanceof SelectorExpr selectorExpr)
            return getPackage(selectorExpr.x()).getPackage(selectorExpr.sel());
        else
            throw new ParsingException("Invalid package name: " + qualifiedName.getText());
    }

    @Override
    public Name getName() {
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
            log.trace("Package: {}", pkg.getQualName());
            for (Clazz aClass : pkg.getClasses()) {
                log.trace("Class: {}", aClass.getName());
            }
        });

    }

}
