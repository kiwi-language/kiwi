package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.List;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.KlassFlags;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public class Clazz extends ElementBase implements Member, ClassScope, GenericDecl, ClassType, ConstPoolOwner {

    private ClassTag tag;
    private Name name;
    private Name qualName;
    private Integer sourceTag;
    private Access access;
    private List<ClassType> interfaces = List.nil();
    private final ClassScope scope;
    private List<TypeVar> typeParams = List.nil();
    private List<Field> fields = List.nil();
    private @Nullable Field summaryField;
    private List<Method> methods = List.nil();
    private List<Clazz> classes = List.nil();
    private List<EnumConst> enumConsts = List.nil();
    private final ConstPool constPool = new ConstPool();
    private final Map<List<Type>, ClassInst> instances = new HashMap<>();
    private List<Attribute> attributes = List.nil();
    private boolean searchable;
    private boolean ephemeral;
    private boolean static_;
    private int rank = -1;
    private @Nullable ElementTable table;
    private List<Type> allTypeArguments;
    private @Nullable Closure closure;
    private @Nullable Method primaryInit;

    public Clazz(ClassTag tag, String name, Access access, ClassScope scope) {
        this(tag, NameTable.instance.get(name), access, scope);
    }

    public Clazz(ClassTag tag, Name name, Access access, ClassScope scope) {
        this.tag = tag;
        this.name = name;
        this.access = access;
        this.scope = scope;
        scope.addClass(this);
    }

    public boolean isAnonymous() {
        return name == NameTable.instance.empty;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
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

    public List<Type> getAllTypeArguments() {
        if (allTypeArguments == null) {
            if (getOwner() == null)
                allTypeArguments = getTypeArguments();
            else
                allTypeArguments = getOwner().getTypeArguments().concat(getTypeArguments());
        }
        return allTypeArguments;
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

    public boolean isTopLevel() {
        return scope instanceof Package;
    }

    @Override
    public Clazz getDeclClass() {
        if (scope instanceof Clazz k)
            return k;
        throw new AnalysisException("Class " + name + " is not an inner class");
    }

    @Override
    public List<Clazz> getClasses() {
        return classes;
    }

    public Clazz findClass(Name name) {
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
    public List<TypeVar> getTypeParams() {
        return typeParams;
    }

    @Override
    public void addTypeParam(TypeVar typeVar) {
        typeParams = typeParams.append(typeVar);
    }

    @Override
    public String getInternalName(@Nullable Func current) {
        return getQualName().toString();
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
        if (method.isInit() && primaryInit == null)
            primaryInit = method;
    }

    public void onMethodAdded(Method method) {
        instances.values().forEach(type -> type.onMethodAdded(method));
    }

    public Iterator<Method> getMethodsByName(String name) {
        return getMethodsByName(NameTable.instance.get(name));
    }

    public Iterator<Method> getMethodsByName(Name name) {
        return methods.findAll(m -> m.getName().equals(name));
    }

    public Method getMethod(Name name, List<Type> parameterTypes) {
        var method = (Method) getTable().lookupFirst(name, e -> e instanceof Method m
                && m.getParamTypes().equals(parameterTypes));
        if (method == null)
            throw new AnalysisException("Cannot find method " + name + "("
                    + parameterTypes.map(Type::getTypeText).join(",") + ") in class " + getQualName());
        return method;
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
        typeParams.forEach(action);
        fields.forEach(action);
        methods.forEach(action);
        classes.forEach(action);
    }

    @Override
    public void write(ElementWriter writer) {
        writer.write("class " + name);
        if (!typeParams.isEmpty()) {
            writer.write("<");
            writer.write(typeParams);
            writer.write(">");
        }
        if (interfaces.nonEmpty()) {
            writer.write(": ");
            writer.write(interfaces.map(ClassType::getTypeText).join(", "));
        }
        writer.writeln(" {");
        writer.indent();
        for (Field field : fields) {
            writer.write(field);
            writer.writeln();
        }
        for (Method method : methods) {
            writer.write(method);
            writer.writeln();
        }
        for (Clazz clazz : classes) {
            writer.write(clazz);
        }
        writer.deIndent();
        writer.writeln("}");
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
    public ElementTable getTable() {
        if (table == null) {
            table = buildTable();
        }
        return table;
    }

    public void addEnumConstant(EnumConst enumConst) {
        enumConsts = enumConsts.append(enumConst);
    }

    public List<EnumConst> getEnumConstants() {
        return enumConsts;
    }

    public Field findFieldByName(Name name) {
        return (Field) getTable().lookupFirst(name, e -> e instanceof Field);
    }

    public Field getFieldByName(String name) {
        return getFieldByName(NameTable.instance.get(name));
    }

    public Field getFieldByName(Name name) {
        return Objects.requireNonNull(findFieldByName(name), () -> "Field " + name + " not found int class " + name);
    }

    @Override
    public TypeVisitor<Type> getSubstitutor() {
        return TypeSubst.empty;
    }

    @Override
    public ClassType getInst(List<Type> typeArgs) {
        if (typeParams.equals(typeArgs))
            return this;
        var owner = scope instanceof Clazz c ? c : null;
        return getInst(owner, typeArgs);
    }

    public ClassType getInst(@Nullable ClassType owner, List<? extends Type> typeArgs) {
        if (owner == getOwner() && typeParams.equals(typeArgs))
            return this;
        List<Type> typeArgs1 = List.into(typeArgs);
        assert scope instanceof Clazz == (owner != null);
        return instances.computeIfAbsent(
                owner != null ? typeArgs1.prepend(owner) : typeArgs1,
                k -> new ClassInst(owner, this, typeArgs1)
        );
    }

    public void traceTypes() {
        var inner = isInner();
        instances.forEach((typeArgs, type) -> {
            ClassType owner;
            if (inner) {
                owner = (ClassType) typeArgs.head();
                typeArgs = typeArgs.tail();
            }
            else
                owner = null;
            log.trace("Owner: {}, type arguments: {}",
                    Utils.safeCall(owner, Type::getTypeText),
                    typeArgs.map(Type::getTypeText)
            );
        });
    }

    public Name getQualName() {
        if (qualName == null) {
            var scopeName = scope.getQualName();
            qualName = scopeName.isEmpty() ? name : scopeName.concat("." + name);
        }
        return qualName;
    }

    @Override
    public int compareTo(@NotNull ClassType that) {
        if (that instanceof Clazz o) {
            if (this == o)
                return 0;
            if (tag != o.tag)
                return tag.compareTo(o.tag);
            var r = Integer.compare(o.getRank(), getRank());
            if (r != 0)
                return r;
            return getQualName().compareTo(o.getQualName());
        }
        else
            return ClassType.super.compareTo(that);
    }

    @Override
    public List<Type> getTypeArguments() {
        return List.into(typeParams);
    }

    public boolean isInterface() {
        return tag == ClassTag.INTERFACE;
    }

    public boolean isValue() {
        return tag == ClassTag.VALUE;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public ClassType getOwner() {
        return scope instanceof Clazz c ? c : null;
    }

    @Override
    public Clazz getClazz() {
        return this;
    }

    public boolean isInner() {
        return scope instanceof Clazz;
    }

    public ClassTag getClassTag() {
        return tag;
    }

    public boolean isEnum() {
        return tag == ClassTag.ENUM;
    }

    public boolean isEntity() {
        return tag == ClassTag.CLASS;
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

    public ConstPool getConstPool() {
        return constPool;
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

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public boolean isStatic() {
        return static_;
    }

    public void setStatic(boolean static_) {
        this.static_ = static_;
    }

    public int getFlags() {
        var flags = 0;
        if (searchable)
            flags |= KlassFlags.FLAG_SEARCHABLE;
        if (ephemeral || isLocal())
            flags |= KlassFlags.FLAG_EPHEMERAL;
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

    public boolean isLocal() {
        return scope instanceof Func;
    }

    public boolean isSubclassOf(Clazz c) {
        if (this == c)
            return true;
        for (var anInterface : interfaces) {
            if (anInterface.getClazz().isSubclassOf(c))
                return true;
        }
        return false;
    }

    @Nullable
    public Field getSummaryField() {
        return summaryField;
    }

    public void setSummaryField(@Nullable Field field) {
        this.summaryField = field;
    }

    @Override
    public String toString() {
        return "Class " + getQualName();
    }

    @Override
    public @Nullable Method getPrimaryInit() {
        return primaryInit;
    }

    public boolean isBean() {
        return attributes.anyMatch(attr -> attr.name().equals(AttributeNames.BEAN_NAME));
    }
}
