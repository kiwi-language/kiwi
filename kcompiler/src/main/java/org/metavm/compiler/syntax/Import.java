package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.Element;
import org.metavm.compiler.element.Project;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public class Import extends Node {

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

    public void resolve(Project project) {
        var pkg = project.getPackage(name.x());
        elements = List.of(pkg.getClass(name.sel()));
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
