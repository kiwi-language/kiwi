package org.metavm.compiler.type;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.Node;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public class ClassInst implements ClassType, Element, GenericDecl {
    @Nullable
    private final ClassType owner;
    private final Clazz clazz;
    private final List<Type> typeArguments;

    private @Nullable List<Type> allTypeArguments;

    private @Nullable List<ClassType> interfaces;
    private @Nullable List<FieldInst> fields;
    private @Nullable List<PartialMethodInst> methods;
    private @Nullable List<ClassType> classes;

    private MethodInst primInit;

    private @Nullable ElementTable table;
    private @Nullable TypeSubst substitutor;
    private @Nullable Closure closure;

    public ClassInst(@Nullable ClassType owner, Clazz clazz, List<Type> typeArguments) {
        this.typeArguments = typeArguments;
        this.owner = owner;
        this.clazz = clazz;
    }

    @Override
    public Name getName() {
        return clazz.getName();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitClassInst(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {

    }

    @Override
    public void write(ElementWriter writer) {
        writeType(writer);
    }

    @Override
    public Node getNode() {
        return null;
    }

    @Override
    public void setNode(Node node) {

    }

    public @Nullable ClassType getSuperType() {
        return getInterfaces().find(t -> !t.isInterface());
    }

    public List<ClassType> getInterfaces() {
        if (interfaces == null)
            interfaces = clazz.getInterfaces().map(t -> (ClassType) t.accept(getSubstitutor()));
        return interfaces;
    }

    public List<FieldInst> getFields() {
        if (fields == null)
            fields = clazz.getFields().map(this::createFieldRef);
        return fields;
    }

    @Override
    public List<? extends ClassType> getClasses() {
        if (classes == null)
            classes = clazz.getClasses().map(c -> c.getInst(this, c.getTypeParams()));
        return classes;
    }

    @Override
    public MethodRef getPrimaryInit() {
        var init = (MethodRef) getTable().lookupFirst(Name.init(),
                e -> e instanceof PartialMethodInst m && m.getFunc() == getClazz().getPrimaryInit()
        );
        return Objects.requireNonNull(init);
    }

    @Override
    public boolean isEnum() {
        return clazz.isEnum();
    }

    public List<PartialMethodInst> getMethods() {
        if (methods == null)
            methods = clazz.getMethods().map(this::createMethodInst);
        return methods;
    }

    public MethodInst getMethod(Name name, List<Type> parameterTypes) {
        return (MethodInst) Objects.requireNonNull(getTable().lookupFirst(name, e -> e instanceof MethodInst m
                && m.getParamTypes().equals(parameterTypes)),
                () -> "Cannot find method " + name + "(" + parameterTypes.map(Type::getTypeText).join(",")
                        + ") in class type " + this.getTypeText());
    }

    private FieldInst createFieldRef(Field field) {
        var type = field.getType().accept(getSubstitutor());
        return new FieldInst(this, field, type);
    }

    private PartialMethodInst createMethodInst(Method method) {
        return new PartialMethodInst(this, method);
    }

    @Override
    public <R> R accept(TypeVisitor<R> visitor) {
        return visitor.visitClassType(this);
    }

    @Override
    public int getTag() {
        return TypeTags.TAG_CLASS;
    }

    @Override
    @Nullable
    public ClassType getOwner() {
        return owner;
    }

    public Clazz getClazz() {
        return clazz;
    }

    @Override
    public List<Type> getTypeArguments() {
        return typeArguments;
    }

    public TypeSubst getSubstitutor() {
        if (substitutor == null) {
            var map = new HashMap<Type, Type>();
            buildTypeArgMap(map);
            substitutor = TypeSubst.create(map);
        }
        return substitutor;
    }

    @Override
    public String toString() {
        return "ClassType " + this.getTypeText();
    }

    public ElementTable getTable() {
        if (table == null)
            table = buildTable();
        return table;
    }

    @Override
    public List<TypeVar> getTypeParams() {
        return clazz.getTypeParams();
    }

    @Override
    public void addTypeParam(TypeVar typeVar) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getInternalName(@Nullable Func current) {
        if (!typeArguments.isEmpty())
            return clazz.getQualName()
                    + "<" + typeArguments.map(type -> type.getInternalName(current)).join(",") + ">";
        else
            return clazz.getQualName().toString();
    }

    public boolean isInterface() {
        return clazz.isInterface();
    }

    public void onMethodAdded(Method method) {
        getTable().add(createMethodInst(method));
    }

    @Override
    public void write(MvOutput output) {
        if (owner == null && typeArguments.isEmpty() && !isLocal()) {
            output.write(ConstantTags.CLASS_TYPE);
            Elements.writeReference(clazz, output);
        } else {
            output.write(ConstantTags.PARAMETERIZED_TYPE);
            if (owner != null)
                owner.write(output);
            else if (isLocal()) {
                var func = (Func) clazz.getScope();
                func.write(output);
            }
            else
                output.write(ConstantTags.NULL);
            Elements.writeReference(clazz, output);
            output.writeList(typeArguments, t -> t.write(output));
        }
    }

    public boolean isLocal() {
        return clazz.isLocal();
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

    public ClassType getInst(List<Type> typeArguments) {
        return clazz.getInst(owner, typeArguments);
    }
}
