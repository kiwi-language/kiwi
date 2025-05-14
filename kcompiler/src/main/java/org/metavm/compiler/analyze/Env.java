package org.metavm.compiler.analyze;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.Types;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

@Slf4j
public class Env {

    private final Project project;
    private final Types types = Types.instance;
    private final ElementTable table = new ElementTable();
    private Scope currentScope;

    public Env(Project project) {
        this.project = project;
    }


    public Project getProject() {
        return project;
    }

    public Scope enterScope(Node node) {
        return currentScope = new Scope(node, null, table, this, currentScope);
    }

    public Scope enterScope(Node node, Element element) {
        return currentScope = new Scope(node, element, table, this, currentScope);
    }

    void setCurrentScope(Scope currentScope) {
        this.currentScope = currentScope;
    }

    public Scope currentScope() {
        return Objects.requireNonNull(currentScope);
    }

    @Nullable Element lookupFirst(Name name) {
        return table.lookupFirst(name);
    }

    public @Nullable Element lookupFirst(Name name, Predicate<Element> filter) {
        return table.lookupFirst(name ,filter);
    }

    public @Nullable Element lookupFirst(Name name, EnumSet<ResolveKind> kinds) {
        if (kinds.contains(ResolveKind.VAR)) {
            var found = table.lookupFirst(name, e -> e instanceof Variable);
            if (found != null)
                return found;
        }
        if (kinds.contains(ResolveKind.TYPE)) {
            var found = table.lookupFirst(name, e -> e instanceof ClassType || e instanceof TypeVar);
            if (found != null)
                return found;
        }
        if (kinds.contains(ResolveKind.PACKAGE)) {
            return table.lookupFirst(name, e -> e instanceof Package);
        }
        return null;
    }

    Iterable<Element> lookupAll(Name name) {
        return table.lookupAll(name);
    }

    <E extends Element> Iterator<E> lookupAll(Name name, Class<E> clazz) {
        //noinspection unchecked
        return (Iterator<E>) table.lookupAll(name, clazz::isInstance);
    }

    public Type resolveType(TypeNode typeNode) {
        return typeNode.resolve(this);
    }

    public Types types() {
        return types;
    }

    public ElementTable getTable() {
        return table;
    }

    public Executable currentExecutable() {
        for (var s = currentScope; s != null; s = s.getParent()) {
            if (s.getElement() instanceof Executable exe)
                return exe;
        }
        throw new IllegalStateException("Not inside any executable");
    }

    public Func currentFunc() {
        for (var s = currentScope; s != null; s = s.getParent()) {
            if (s.getElement() instanceof Func f)
                return f;
        }
        throw new IllegalStateException("Not inside any method");
    }

    public Clazz currentClass() {
        for (var s = currentScope; s != null; s = s.getParent()) {
            if (s.getElement() instanceof Clazz c)
                return c;
        }
        throw new IllegalStateException("Not inside any class");
    }

    public Method currentMethod() {
        for (var s = currentScope; s != null; s = s.getParent()) {
            if (s.getElement() instanceof Method m)
                return m;
        }
        throw new IllegalStateException("Not inside any method");
    }

    public int getContextIndex(Executable executable) {
        var s = currentScope;
        var idx = -1;
        while (s != null) {
            if (s.getElement() instanceof Executable exe) {
                if (exe == executable)
                    return idx;
                else
                    idx++;
            }
            s = s.getParent();
        }
        throw new IllegalStateException("Not inside executable: " + executable);
    }

    public int getParentIndex(Clazz clazz) {
        var s = currentScope;
        var idx = - 1;
        while (s != null) {
            if (s.getElement() instanceof Clazz c) {
                if (c.isSubclassOf(clazz))
                    return idx;
                else
                    idx++;
            }
            s = s.getParent();
        }
        throw new IllegalStateException("Not inside class: " + clazz.getQualName());
    }

    public Node findJumpTarget(@Nullable Name label) {
        var s = currentScope;
        while (s != null) {
            if (label == null) {
                if (s.getNode() instanceof ForeachStmt foreachStmt)
                    return foreachStmt;
                if (s.getNode() instanceof WhileStmt whileStmt)
                    return whileStmt;
                if (s.getNode() instanceof DoWhileStmt doWhileStmt)
                    return doWhileStmt;
            }
            else if (s.getNode() instanceof LabeledStmt labeledStmt && labeledStmt.getLabel() == label)
                return labeledStmt;
            s = s.getParent();
        }
        throw new AnalysisException("Cannot find jump target, label: " + label);
    }

}
