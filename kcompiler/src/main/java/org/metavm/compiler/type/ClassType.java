package org.metavm.compiler.type;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

@Slf4j
public final class ClassType implements Type, Comparable<ClassType> {
    @Nullable
    private final ClassType owner;
    private final Clazz clazz;
    private final List<Type> typeArguments;

    private @Nullable List<Type> allTypeArguments;

    private @Nullable List<ClassType> interfaces;
    private @Nullable List<FieldInst> fields;
    private @Nullable List<MethodInst> methods;

    private @Nullable ElementTable table;
    private @Nullable TypeSubstitutor substitutor;
    private @Nullable Closure closure;

    public ClassType(@Nullable ClassType owner, Clazz clazz, List<Type> typeArguments) {
        this.typeArguments = typeArguments;
        this.owner = owner;
        this.clazz = clazz;
    }

    @Override
    public void write(ElementWriter writer) {
        if (owner != null) {
            writer.writeType(owner);
            writer.write(".");
        }
        writer.write(clazz.getName());
        if (!typeArguments.isEmpty()) {
            writer.write("<");
            writer.writeTypes(typeArguments);
            writer.write(">");
        }
    }

    public ClassType asSuper(Clazz superClass) {
        return asSuper(superClass, new HashSet<>());
    }

    private ClassType asSuper(Clazz superClass, Set<Clazz> seen) {
        if (!seen.add(clazz))
            return null;
        if (superClass == clazz)
            return this;
        for (ClassType it : getInterfaces()) {
            var found = it.asSuper(superClass, seen);
            if (found != null)
                return found;
        }
        return null;
    }

    public @Nullable ClassType getSuperType() {
        return getInterfaces().find(t -> !t.isInterface());
    }

    public List<ClassType> getInterfaces() {
        if (interfaces == null) {
            var allTypeArgs = getAllTypeArguments();
            if (allTypeArgs.isEmpty())
                interfaces = clazz.getInterfaces();
            else
                interfaces = clazz.getInterfaces().map(t -> (ClassType) t.accept(getSubstitutor()));
        }
        return interfaces;
    }

    public List<FieldInst> getFields() {
        if (fields == null) {
            fields = List.nil();
            clazz.getFields().forEachBackwards(f ->
                    fields = new List<>(createFieldRef(f), fields)
            );
        }
        return fields;
    }

    public List<MethodInst> getMethods() {
        if (methods == null) {
            methods = List.nil();
            clazz.getMethods().forEachBackwards(m -> methods = new List<>(createMethodInst(m), methods));
        }
        return methods;
    }

    public MethodInst getMethod(SymName name, List<Type> parameterTypes) {
        return (MethodInst) Objects.requireNonNull(getTable().lookupFirst(name, e -> e instanceof MethodInst m
                && m.getParameterTypes().equals(parameterTypes)),
                () -> "Cannot find method " + name + "(" + parameterTypes.map(Type::getText).join(",")
                        + ") in class type " + getText());
    }

    private FieldInst createFieldRef(Field field) {
        var type = field.getType().accept(getSubstitutor());
        return new FieldInst(this, field, type);
    }

    private MethodInst createMethodInst(Method method) {
        var retType = method.getReturnType().accept(getSubstitutor());
        var paramTypes = method.getParameters().map(p -> p.getType().accept(getSubstitutor()));
        return new MethodInst(this, method, List.into(method.getTypeParameters()),
                Types.instance.getFunctionType(paramTypes, retType));
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        type = type.getUpperBound();
        if (type == this || type == PrimitiveType.NEVER)
            return true;
        if (type instanceof ClassType that) {
            var s = that.asSuper(clazz);
            if (s == null)
                return false;
            if (owner != null && (s.owner == null || owner.isAssignableFrom(s.owner)))
                return false;
            if (typeArguments.size() != s.typeArguments.size())
                return false;
            var it1 = typeArguments.iterator();
            var it2 = s.typeArguments.iterator();
            while (it1.hasNext()) {
                if (!it1.next().contains(it2.next()))
                    return false;
            }
            return true;
        }
        else
            return false;
    }

    @Override
    public <R> R accept(TypeVisitor<R> visitor) {
        return visitor.visitClassType(this);
    }

    @Override
    public int getTag() {
        return TypeTags.TAG_CLASS;
    }

    @Nullable
    public ClassType getOwner() {
        return owner;
    }

    public Clazz getClazz() {
        return clazz;
    }

    public List<Type> getTypeArguments() {
        return typeArguments;
    }

    public List<Type> getAllTypeArguments() {
        if (allTypeArguments == null) {
            if (owner == null)
                allTypeArguments = typeArguments;
            else
                allTypeArguments = owner.getAllTypeArguments().concat(typeArguments);
        }
        return allTypeArguments;
    }

    public TypeSubstitutor getSubstitutor() {
        if (substitutor == null) {
            var allTypeParams = clazz.getType().getAllTypeArguments();
            var allTypeArgs = getAllTypeArguments();
            substitutor = new TypeSubstitutor(allTypeParams, allTypeArgs);
        }
        return substitutor;
    }

    @Override
    public String toString() {
        return "ClassType " + getText();
    }

    @Override
    public int compareTo(@NotNull ClassType that) {
        var r = clazz.compareTo(that.clazz);
        if (r != 0)
            return r;
        if (owner != null) {
            assert that.owner != null;
            r = owner.compareTo(that.owner);
            if (r != 0)
                return r;
        }
        return Types.instance.compareTypes(typeArguments, that.typeArguments);
    }

    public ElementTable getTable() {
        if (table == null)
            buildTable();
        return table;
    }

    @Override
    public String getInternalName(@Nullable Func current) {
        if (!typeArguments.isEmpty())
            return clazz.getQualifiedName()
                    + "<" + typeArguments.map(type -> type.getInternalName(current)).join(",") + ">";
        else
            return clazz.getQualifiedName().toString();
    }

    @Override
    public ClassTypeNode makeNode() {
        var name = makeNameNode();
        var expr = typeArguments.nonEmpty() ?
                new TypeApply(name, typeArguments.map(Type::makeNode)) : name;
        return new ClassTypeNode(expr);
    }

    private Name makeNameNode() {
        if (owner != null)
            return new QualifiedName(owner.makeNameNode(), new Ident(clazz.getName()));
        else
            return new Ident(clazz.getName());
    }

    public boolean isInterface() {
        return clazz.isInterface();
    }

    private void buildTable() {
        if (Traces.traceElementTable) {
            log.trace("Building element table for {}, super: {}, interfaces: {}",
                    getText(), Utils.safeCall(getSuperType(), Type::getText),
                    getInterfaces().map(Type::getText).join(",")
            );
        }
        table = new ElementTable();
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
        for (ClassType it : allInterfaces) {
            table.addAll(it.getTable());
        }
        for (ClassType s : allSuperTypes) {
            table.addAll(s.getTable());
        }
        table.add(new BuiltinVariable(SymNameTable.instance.this_, null, this));
        var s = getSuperType();
        if (s != null) {
            table.add(new BuiltinVariable(SymNameTable.instance.super_, null, s));
            var inits =
                    s.getTable().lookupAll(SymName.init(), e -> e instanceof MethodInst m && m.isConstructor());
            inits.forEach(i -> {
                var method = (MethodInst) i;
                table.add(new BuiltinVariable(SymName.super_(), method, method.getType()));
            });
        }
        clazz.getClasses().forEach(table::add);
        getMethods().forEach(table::add);
        clazz.getEnumConstants().forEach(table::add);
        getFields().forEach(table::add);
        table.freeze();
        if (Traces.traceElementTable)
            log.trace("Element table for class type {}: {}", getText(), table);
    }

    @Override
    public void write(MvOutput output) {
        if (owner == null && typeArguments.isEmpty()) {
            output.write(ConstantTags.CLASS_TYPE);
            Elements.writeReference(clazz, output);
        } else {
            output.write(ConstantTags.PARAMETERIZED_TYPE);
            if (owner != null)
                owner.write(output);
            else
                output.write(ConstantTags.NULL);
            Elements.writeReference(clazz, output);
            output.writeList(typeArguments, t -> t.write(output));
        }
    }

    @Override
    public Closure getClosure() {
        if (closure == null) {
            var cl = Closure.nil;
            for (var t : getInterfaces()) {
                cl = cl.union(t.getClosure());
            }
            closure = cl.insert(this);
        }
        return closure;
    }

    public int getRank() {
        return clazz.getRank();
    }

}
