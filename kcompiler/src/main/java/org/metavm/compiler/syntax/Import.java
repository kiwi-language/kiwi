package org.metavm.compiler.syntax;

import org.metavm.compiler.diag.Errors;
import org.metavm.compiler.diag.Log;
import org.metavm.compiler.element.Element;
import org.metavm.compiler.element.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Import extends Node {

    public static final Logger logger = LoggerFactory.getLogger(Import.class);

    private SelectorExpr name;

    private List<Element> elements;

    public Import(SelectorExpr name) {
        this.name = name;
    }

    public SelectorExpr getName() {
        return name;
    }

    public void setName(SelectorExpr name) {
        this.name = name;
    }

    public void resolve(Project project, Log log) {
        var pkg = project.findPackage(name.x());
        if (pkg == null) {
            elements = List.of();
            log.error(name.x(), Errors.symbolNotFound);
            return;
        }
        var cls = pkg.findClass(name.sel());
        if (cls != null)
            elements = List.of(cls);
        else {
            elements = List.of();
            log.error(name, Errors.symbolNotFound);
        }

    }

    public List<Element> getElements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("import ");
        writer.write(name);
        writer.writeln();
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitImport(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(name);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Import anImport = (Import) object;
        return Objects.equals(name, anImport.name) && Objects.equals(elements, anImport.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, elements);
    }
}
