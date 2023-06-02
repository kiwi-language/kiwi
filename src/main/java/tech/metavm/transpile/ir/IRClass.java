package tech.metavm.transpile.ir;

import tech.metavm.entity.NoProxy;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

public class IRClass extends IRType implements ISymbol, InternalGenericDeclaration<IRClass> {

    private final IRPackage pkg;
    private final Set<Modifier> modifiers;
    private final List<IRAnnotation> annotations;
    private final @Nullable IRClass declaringClass;
    private final @Nullable IRMethod declaringMethod;
    private @Nullable IRType superType;
    private List<IRType> interfaces;
    private final List<TypeVariable<IRClass>> typeParameters = new ArrayList<>();

    private final IRClassKind kind;

    private final List<IRField> fields = new ArrayList<>();
    private final List<IRMethod> methods = new ArrayList<>();
    private final List<IRClass> classes = new ArrayList<>();
    private final List<CodeBlock> staticBlocks = new ArrayList<>();
    private final List<IRConstructor> constructors = new ArrayList<>();

    public IRClass(
            @Nullable String name,
            IRClassKind kind,
            IRPackage pkg,
            List<Modifier> modifiers,
            List<IRAnnotation> annotations,
            @Nullable IRClass declaringClass,
            @Nullable IRMethod declaringMethod
    ) {
        super(pkg.getClassName(name));
        this.kind = kind;
        this.pkg = pkg;
        this.declaringClass = declaringClass;
        this.declaringMethod = declaringMethod;
        this.modifiers = new HashSet<>(modifiers);
        this.annotations = new ArrayList<>(annotations);
        if(declaringClass != null) {
            declaringClass.addClass(this);
        }
    }

    public void addClass(IRClass klass) {
        this.classes.add(klass);
    }

    public List<IRAnnotation> getAnnotations() {
        return annotations;
    }

    @Nullable
    public IRClass getDeclaringClass() {
        return declaringClass;
    }

    public List<TypeVariable<IRClass>> typeParameters() {
        return typeParameters;
    }

    public TypeVariable<IRClass> typeParameter(int index) {
        return typeParameters.get(index);
    }

    public int getTypeParameterIndex(TypeVariable<IRClass> typeParameter) {
        int idx = typeParameters.indexOf(typeParameter);
        if(idx < 0) {
            throw new InternalException("Type parameter " + typeParameter + " is not defined in " + this);
        }
        return idx;
    }

    @NoProxy
    public IRPackage getPkg() {
        return pkg;
    }

    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    public List<IRMethod> methods() {
        return methods;
    }

    public List<IRField> fields() {
        return fields;
    }

    public List<IRField> staticFields() {
        return NncUtils.filter(fields, IRField::isStatic);
    }

    public List<IRMethod> staticMethods() {
        return NncUtils.filter(methods, IRMethod::isStatic);
    }

    public List<IRClass> staticClasses() {
        return NncUtils.filter(classes, IRClass::isStatic);
    }

    public List<IRClass> classes() {
        return classes;
    }

    public IRClass getClass(String name) {
        return NncUtils.findRequired(classes, c -> name.equals(c.getName()));
    }


    public IRField tryGetField(String name) {
        return isFieldDeclared(name) ? getField(name) : null;
    }

    public IRMethod tryGetMethod(String name, List<IRType> parameterTypes) {
        return isMethodDeclared(name, parameterTypes) ? getMethod(name, parameterTypes) : null;
    }

    public IRClass tryGetClass(String name) {
        return isClassDeclared(name) ? getClass(name) : null;
    }


    @Nullable
    public IRType getSuperType() {
        return superType;
    }

    public IRClass getRawSuperClass() {
        if(superType == null) {
            throw new NullPointerException("Type '" + this + " does not have a super type");
        }
        if(superType instanceof IRClass klass) {
            return klass;
        }
        else if(superType instanceof PType pType) {
            return pType.getRawClass();
        }
        else {
            throw new InternalException("Invalid super type " + superType);
        }
    }

    public List<IRType> getInterfaces() {
        return interfaces;
    }

    public boolean isPublic() {
        return modifiers.contains(Modifier.PUBLIC);
    }

    public boolean isStatic() {
        return modifiers.contains(Modifier.STATIC);
    }

    public List<CodeBlock> getStaticBlocks() {
        return staticBlocks;
    }

    public List<IRConstructor> getConstructors() {
        return constructors;
    }

    public void addField(IRField field) {
        this.fields.add(field);
    }

    public void addMethod(IRMethod method) {
        methods.add(method);
    }

    public void addConstructor(IRConstructor constructor) {
        this.constructors.add(constructor);
    }

    public void addStaticBlock(CodeBlock block) {
        staticBlocks.add(block);
    }

    public void setSuperType(@Nullable IRType superType) {
        this.superType = superType;
    }

    public void setInterfaces(List<IRType> interfaces) {
        this.interfaces = new ArrayList<>(interfaces);
    }

    @Nullable
    public IRMethod getDeclaringMethod() {
        return declaringMethod;
    }

    public boolean isMethodDeclared(String name) {
        return NncUtils.exists(methods, m -> m.name().equals(name));
    }

    public boolean isMethodDeclared(String name, List<IRType> parameterTypes) {
        return NncUtils.exists(methods, m -> m.matches(name, parameterTypes));
    }

    public boolean isFieldDeclared(String name) {
        return NncUtils.exists(fields, f -> f.name().equals(name));
    }

    public boolean isClassDeclared(String name) {
        return NncUtils.exists(classes, c -> c.name().equals(name));
    }

    public IRMethod getMethod(String name, List<IRType> parameterTypes) {
        return NncUtils.findRequired(methods, m -> m.matches(name, parameterTypes));
    }

    public List<IRMethod> getMethods(String name) {
        return NncUtils.filter(methods, m -> m.name().equals(name));
    }

    public IRConstructor getConstructor(List<IRType> parameterTypes) {
        return NncUtils.findRequired(
                constructors,
                c -> Objects.equals(c.parameterTypes(), parameterTypes)
        );
    }

    public IRField getField(String name) {
        return NncUtils.findRequired(fields, f -> f.name().equals(name));
    }

    @Override
    @NoProxy
    public String name() {
        return getSimpleName();
    }

    public boolean isInterface() {
        return kind == IRClassKind.INTERFACE;
    }

    public boolean isEnum() {
        return kind == IRClassKind.ENUM;
    }

    public boolean isRecord() {
        return kind == IRClassKind.RECORD;
    }

    public boolean isAnnotation() {
        return kind == IRClassKind.ANNOTATION;
    }

    @Override
    public void addTypeParameter(TypeVariable<IRClass> typeVariable) {
        typeParameters.add(typeVariable);
    }

    @Override
    public boolean isAssignableFrom0(IRType that) {
        if(that instanceof IRAnyType) {
            return true;
        }
        if(that instanceof IRClass thatClass) {
            if(this == thatClass) {
                return true;
            }
            if(thatClass.superType != null && isAssignableFrom(thatClass.superType)) {
                return true;
            }
            return NncUtils.anyMatch(thatClass.interfaces, this::isAssignableFrom);
        }
        if(that instanceof PType pType) {
            return isAssignableFrom(pType.getRawClass());
        }
        if(that instanceof TypeVariable<?> typeVariable) {
            return NncUtils.anyMatch(
                    typeVariable.getUpperBounds(),
                    this::isAssignableFrom
            );
        }
        if(that instanceof TypeIntersection typeIntersection) {
            return NncUtils.anyMatch(
                    typeIntersection.getTypes(),
                    this::isAssignableFrom
            );
        }
        if(that instanceof TypeUnion typeUnion) {
            return NncUtils.allMatch(
                    typeUnion.getTypes(),
                    this::isAssignableFrom
            );
        }
        return false;
    }

    @Override
    public List<IRType> getReferences() {
        return List.of();
    }

    @Override
    public IRType cloneWithReferences(Map<IRType, IRType> referenceMap) {
        return this;
    }

    public List<IRType> getSupers() {
        var result = new ArrayList<IRType>();
        if(superType != null) {
            result.add(superType);
        }
        result.addAll(interfaces);
        return result;
    }

    public IRType getSuper(IRClass klass) {
        return NncUtils.findRequired(getSupers(), s -> IRUtil.getRawClass(s).equals(klass));
    }

    public boolean hasTypeParameters() {
        return !typeParameters.isEmpty();
    }

    public IRType templateOrSelf() {
        return hasTypeParameters() ? templatePType() : this;
    }

    public PType templatePType() {
        if(typeParameters.isEmpty()) {
            throw new InternalException(this + " does not have type parameters");
        }
        return new PType(
                NncUtils.get(declaringClass, IRClass::templatePType),
                this,
                new ArrayList<>(typeParameters)
        );
    }

}
