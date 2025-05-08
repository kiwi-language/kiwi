package org.metavm.compiler.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;
import org.metavm.util.LinkedList;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface ClassType extends Type, Element, Comparable<ClassType> {
    
    Logger log = LoggerFactory.getLogger(ClassType.class);
    
    List<Type> getTypeArguments();

    boolean isInterface();

    @Nullable
    ClassType getOwner();

    Clazz getClazz();

    int getRank();

    default int compareTo(@NotNull ClassType that) {
        var r = getClazz().compareTo(that.getClazz());
        if (r != 0)
            return r;
        if (getOwner() != null) {
            assert that.getOwner() != null;
            r = getOwner().compareTo(that.getOwner());
            if (r != 0)
                return r;
        }
        return Types.instance.compareTypes(getTypeArguments(), that.getTypeArguments());
    }

    @Override
    default ClassTypeNode makeNode() {
        var expr = makeNameNode();
        var typeArgs = getTypeArguments();
        if (!typeArgs.equals(getClazz().getTypeArguments()))
            expr = new TypeApply(expr, typeArgs.map(Type::makeNode));
        var node = new ClassTypeNode(expr);
        node.setType(this);
        return node;
    }

    default Expr makeNameNode() {
        var owner = getOwner();
        return owner != null ?
                new SelectorExpr(owner.makeNameNode(), getName()) :
                new Ident(getName());
    }

    List<ClassType> getInterfaces();

    default @Nullable ClassType asSuper(Clazz superClass) {
        return asSuper(superClass, new HashSet<>());
    }

    default ClassType asSuper(Clazz clazz, Set<Clazz> seen) {
        if (!seen.add(getClazz()))
            return null;
        if (getClazz() == clazz)
            return this;
        for (var it : getInterfaces()) {
            var found = it.asSuper(clazz, seen);
            if (found != null)
                return found;
        }
        return null;
    }

    @Override
    default boolean isAssignableFrom(Type type) {
        type = type.getUpperBound();
        if (type == this || type == PrimitiveType.NEVER) {
            return true;
        }
        if (type instanceof ClassType that) {
            var s = that.asSuper(getClazz());
            if (s == null) {
                return false;
            }
            if (getOwner() != null && (s.getOwner() == null || !getOwner().isAssignableFrom(s.getOwner()))) {
                return false;
            }
            if (getTypeArguments().size() != s.getTypeArguments().size()) {
                return false;
            }
            var it1 = getTypeArguments().iterator();
            var it2 = s.getTypeArguments().iterator();
            while (it1.hasNext()) {
                if (!it1.next().contains(it2.next())) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    default @Nullable ClassType getSuperType() {
        return getInterfaces().find(t -> !t.isInterface());
    }

    default ElementTable buildTable() {
        if (Traces.traceElementTable) {
            log.trace("Building element table for {}, super: {}, interfaces: {}",
                    this.getTypeText(), Utils.safeCall(getSuperType(), Type::getTypeText),
                    getInterfaces().map(Type::getTypeText).join(",")
            );
        }
        var table = new ElementTable();
        var queue = new LinkedList<ClassType>();
        queue.offer(this);
        var allInterfaces = new LinkedList<ClassType>();
        var allSuperTypes = new LinkedList<ClassType>();
        while (!queue.isEmpty()) {
            var t = queue.poll();
            if (t != this) {
                if (t.isInterface())
                    allInterfaces.addLast(t);
                else
                    allSuperTypes.addLast(t);
            }
            t.getInterfaces().forEach(queue::offer);
        }
        for (var it : allInterfaces) {
            table.addAll(it.getTable());
        }
        for (var s : allSuperTypes) {
            table.addAll(s.getTable());
        }
        table.add(new BuiltinVariable(NameTable.instance.this_, null, this));
        var s = getSuperType();
        if (s != null) {
            table.add(new BuiltinVariable(NameTable.instance.super_, null, s));
        }
        getClasses().forEach(table::add);
        getMethods().forEach(table::add);
        getClazz().getEnumConstants().forEach(table::add);
        getFields().forEach(table::add);
        table.freeze();
        if (Traces.traceElementTable)
            log.trace("Element table for class type {}: {}", this.getTypeText(), table);
        return table;
    }

    default void buildTypeArgMap(Map<Type, Type> map) {
        if (getOwner() != null)
            getOwner().buildTypeArgMap(map);
        var it1 = getClazz().getTypeParams().iterator();
        var it2 = getTypeArguments().iterator();
        while (it1.hasNext() && it2.hasNext())
            map.put(it1.next(), it2.next());
        assert !it1.hasNext() && !it2.hasNext();
    }

    @Override
    default void write(MvOutput output) {
        if (getOwner() == null && getTypeArguments().isEmpty() && !isLocal()) {
            output.write(ConstantTags.CLASS_TYPE);
            Elements.writeReference(getClazz(), output);
        } else {
            output.write(ConstantTags.PARAMETERIZED_TYPE);
            if (getOwner() != null)
                getOwner().write(output);
            else if (isLocal()) {
                var func = (Func) getClazz().getScope();
                func.write(output);
            }
            else
                output.write(ConstantTags.NULL);
            Elements.writeReference(getClazz(), output);
            output.writeList(getTypeArguments(), t -> t.write(output));
        }
    }

    @Override
    default void writeType(ElementWriter writer) {
        if (getOwner() != null) {
            writer.writeType(getOwner());
            writer.write(".");
        }
        writer.write(getClazz().getName());
        if (!getTypeArguments().isEmpty()) {
            writer.write("<");
            writer.writeTypes(getTypeArguments());
            writer.write(">");
        }
    }

    boolean isLocal();

    @Override
    default String getTypeText() {
        return Type.super.getTypeText();
    }

    ClassType getInst(List<Type> typeArguments);

    TypeVisitor<Type> getSubstitutor();

    List<? extends MethodRef> getMethods();

    List<? extends FieldRef> getFields();

    List<? extends ClassType> getClasses();

    MethodRef getPrimaryInit();

    boolean isEnum();

    default boolean isInner() {
        return getClazz().isInner();
    }

    default boolean isStatic() {
        return getClazz().isStatic();
    }
}
