package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;
import org.metavm.object.type.Klass;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public class Clazz implements Member, ClassScope, GenericDeclaration, Comparable<Clazz> {

    private ClassTag tag;
    private SymName name;
    private Integer sourceTag;
    private Access access;
    private String qualifiedName;
    private List<ClassType> interfaces = List.nil();
    private final ClassScope scope;
    private List<TypeVariable> typeParameters = List.nil();
    private List<Field> fields = List.nil();
    private List<Method> methods = List.nil();
    private List<Clazz> classes = List.nil();
    private List<EnumConstant> enumConstants = List.nil();
    private final ConstantPool constantPool = new ConstantPool();
    private final Map<List<Type>, ClassType> types = new HashMap<>();
    private List<Attribute> attributes = List.nil();
    private boolean searchable;
    private int rank = -1;

    public Clazz(ClassTag tag, String name, Access access, ClassScope scope) {
        this(tag, SymNameTable.instance.get(name), access, scope);
    }

    public Clazz(ClassTag tag, SymName name, Access access, ClassScope scope) {
        this.tag = tag;
        this.name = name;
        this.access = access;
        this.scope = scope;
        scope.addClass(this);
    }

    public SymName getName() {
        return name;
    }

    public void setName(SymName name) {
        this.name = name;
    }

    @Override
    public Access getAccess() {
        return access;
    }

    @Nullable
    public ClassType getSuper() {
        return interfaces.find(c -> !c.isInterface());
    }

    public List<ClassType> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<ClassType> interfaces) {
        this.interfaces = interfaces;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public ClassScope getScope() {
        return scope;
    }

    @Override
    public Clazz getDeclaringClass() {
        if (scope instanceof Clazz k)
            return k;
        throw new AnalysisException("Class " + name + " is not an inner class");
    }

    @Override
    public List<Clazz> getClasses() {
        return classes;
    }

    public Clazz getClass(SymName name) {
        return classes.find(c -> c.name.equals(name));
    }

    public void forEachClass(Consumer<? super Clazz> action) {
        classes.forEach(action);
    }

    @Override
    public void addClass(Clazz clazz) {
        classes = classes.append(clazz);
    }

    @Override
    public List<TypeVariable> getTypeParameters() {
        return typeParameters;
    }

    @Override
    public void addTypeParameter(TypeVariable typeVariable) {
        typeParameters = typeParameters.append(typeVariable);
    }

    @Override
    public Object getInternalName(@Nullable Func current) {
        return getQualifiedName();
    }

    public List<Field> getFields() {
        return fields;
    }

    public void forEachField(Consumer<? super Field> action) {
        var super_ = getSuper();
        if (super_ != null)
            super_.getClazz().forEachField(action);
        fields.forEach(action);
    }

    public void addField(Field field) {
        fields = fields.append(field);
    }

    public void addMethod(Method method) {
        methods = methods.append(method);
    }

    public Iterator<Method> getMethodsByName(String name) {
        return getMethodsByName(SymNameTable.instance.get(name));
    }

    public Iterator<Method> getMethodsByName(SymName name) {
        return methods.findAll(m -> m.getName().equals(name));
    }

    public Method getMethod(SymName name, List<Type> parameterTypes) {
        var methodInst = (MethodInst) getType().getTable().lookupFirst(name, e -> e instanceof MethodInst m
                && m.getFunction().getParameterTypes().equals(parameterTypes));
        if (methodInst == null)
            throw new AnalysisException("Cannot find method " + name + "("
                    + parameterTypes.map(Type::getText).join(",") + ") in class " + getQualifiedName());
        return methodInst.getFunction();
    }

    public List<Method> getMethods() {
        return methods;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitClazz(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {
        typeParameters.forEach(action);
        fields.forEach(action);
        methods.forEach(action);
        classes.forEach(action);
    }

    @Override
    public void write(ElementWriter writer) {
        writer.writeln("class " + name);
        if (!typeParameters.isEmpty()) {
            writer.write("<");
            writer.write(typeParameters);
            writer.write(">");
        }
        writer.writeln(" {");
        writer.indent();
        for (Field field : fields) {
            writer.write(field);
        }
        for (Method method : methods) {
            writer.write(method);
        }
        for (Clazz clazz : classes) {
            writer.write(clazz);
        }
        writer.deIndent();
        writer.writeln("}");
    }

    public void addEnumConstant(EnumConstant enumConstant) {
        enumConstants = enumConstants.append(enumConstant);
    }

    public List<EnumConstant> getEnumConstants() {
        return enumConstants;
    }

    public Field findFieldByName(SymName name) {
        var found = Utils.find(fields, f -> f.getName().equals(name));
        if (found != null) return found;
        var super_ = getSuper();
        if (super_ != null)
            return super_.getClazz().findFieldByName(name);
        return null;
    }

    public Field getFieldByName(String name) {
        return getFieldByName(SymNameTable.instance.get(name));
    }

    public Field getFieldByName(SymName name) {
        return Objects.requireNonNull(findFieldByName(name), () -> "Field " + name + " not found int class " + name);
    }

    public ClassType getType() {
        var owner = scope instanceof Clazz c ? c : null;
        return getType(Utils.safeCall(owner, Clazz::getType), typeParameters);
    }

    public ClassType getType(List<? extends Type> typeArguments) {
        return getType(null, typeArguments);
    }

    public ClassType getType(@Nullable ClassType owner, List<? extends Type> typeArguments) {
        List<Type> typeArgs = List.into(typeArguments);
        assert scope instanceof Clazz == (owner != null);
        return types.computeIfAbsent(
                owner != null ? typeArgs.prepend(owner) : typeArgs,
                k -> new ClassType(owner, this, typeArgs)
        );
    }

    public void traceTypes() {
        var inner = isInnerClass();
        types.forEach((typeArgs, type) -> {
            ClassType owner;
            if (inner) {
                owner = (ClassType) typeArgs.head();
                typeArgs = typeArgs.tail();
            }
            else
                owner = null;
            log.trace("Owner: {}, type arguments: {}",
                    Utils.safeCall(owner, Type::getText),
                    typeArgs.map(Type::getText)
            );
        });
    }

    public SymName getQualifiedName() {
        var scopeName = scope.getQualifiedName();
        return scopeName.isEmpty() ? name : scopeName.concat("." + name);
    }

    public boolean isInnerClass() {
        return scope instanceof Clazz;
    }

    @Override
    public int compareTo(@NotNull Clazz o) {
        if (this == o)
            return 0;
        if (tag != o.tag)
            return tag.compareTo(o.tag);
        var r = Integer.compare(o.getRank(), getRank());
        if (r != 0)
            return r;
        return getQualifiedName().compareTo(o.getQualifiedName());
    }

    public boolean isInterface() {
        return tag == ClassTag.INTERFACE;
    }

    public ClassTag getTag() {
        return tag;
    }

    public boolean isEnum() {
        return tag == ClassTag.ENUM;
    }

    public void setTag(ClassTag tag) {
        this.tag = tag;
    }

    public Integer getSourceTag() {
        return sourceTag;
    }

    public void setSourceTag(Integer sourceTag) {
        this.sourceTag = sourceTag;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public int getSince() {
        return 0;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void setSearchable(boolean b) {
        this.searchable = b;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public int getFlags() {
        var flags = 0;
        if (searchable)
            flags |= Klass.FLAG_SEARCHABLE;
        return flags;
    }

    public int getRank() {
        if (rank < 0) {
            var r = -1;
            for (var s : getInterfaces()) {
                r = Math.max(r, s.getRank());
            }
            rank = r + 1;
        }
        return rank;
    }
}
