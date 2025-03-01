package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.Element;
import org.metavm.compiler.element.Project;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class Import extends Node {

    private QualifiedName name;

    private List<Element> elements;

    public Import(QualifiedName name) {
        this.name = name;
    }

    public QualifiedName getName() {
        return name;
    }

    public void setName(QualifiedName name) {
        this.name = name;
    }

    public void resolve(Project project) {
        var pkg = project.getPackage(name.getQualifier());
        elements = List.of(pkg.getClass(name.getIdent().value()));
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
}
