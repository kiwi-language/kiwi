package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

public class ReflectionClassBuilder {

    private final Class<?> klass;
    private final TypeStore store;
    private IRClass irClass;

    public ReflectionClassBuilder(Class<?> klass, TypeStore store) {
        this.klass = klass;
        this.store = store;
    }

    public IRClass build() {
        irClass = store.internClass(new IRClass(
                klass.getSimpleName(),
                getClassKind(),
                getPackage(),
                getModifiers(klass.getModifiers()),
                getAnnotations(klass.getAnnotations()),
                NncUtils.get(
                        klass.getDeclaringClass(),
                        store::fromClass
                ),
                NncUtils.get(
                        klass.getEnclosingMethod(),
                        store::getMethod
                )
        ));
        if(klass.getSuperclass() != null) {
            irClass.setSuperType(store.fromType(klass.getGenericSuperclass()));
        }
        irClass.setInterfaces(
                NncUtils.map(klass.getGenericInterfaces(), store::fromType)
        );
        buildTypeParameters();
        buildMembers();
        return irClass;
    }

    private void buildMembers() {
        for (Field field : klass.getDeclaredFields()) {
            buildField(field);
        }
        for (Method method : klass.getDeclaredMethods()) {
            buildMethod(method);
        }
        for (Class<?> mClass : klass.getDeclaredClasses()) {
            buildClass(mClass);
        }
    }

    private void buildTypeParameters() {
        for (java.lang.reflect.TypeVariable<? extends Class<?>> typeParameter : klass.getTypeParameters()) {
            buildTypeVariable(typeParameter);
        }
    }

    private void buildTypeVariable(java.lang.reflect.TypeVariable<? extends Class<?>> typeVar) {
        new TypeVariable<>(
                irClass,
                typeVar.getName(),
                NncUtils.map(
                        typeVar.getBounds(), store::fromType
                )
        );
    }

    private void buildField(Field field) {
        new IRField(
                getModifiers(field.getModifiers()),
                getAnnotations(field.getAnnotations()),
                field.getName(),
                store.fromType(field.getGenericType()),
                irClass
        );
    }

    private void buildMethod(Method method) {
        var irMethod = new IRMethod(
                method.getName(),
                getModifiers(method.getModifiers()),
                getAnnotations(method.getAnnotations()),
                irClass
        );
        store.addMethod(irMethod, method);
        parseTypeVariable(method.getTypeParameters());
        irMethod.initialize(
                store.fromType(method.getGenericReturnType()),
                parseParameters(method.getParameters()),
                NncUtils.map(
                        Arrays.asList(method.getExceptionTypes()),
                        store::fromClass
                )
        );
    }

    private List<IRParameter> parseParameters(Parameter[] parameters) {
        return NncUtils.map(
                Arrays.asList(parameters),
                p -> new IRParameter(
                        Modifier.isImmutable(p.getModifiers()),
                        getAnnotations(p.getAnnotations()),
                        p.getName(),
                        store.fromType(p.getParameterizedType()),
                        p.isVarArgs()
                )
        );
    }

    private List<TypeVariable<?>> parseTypeVariable(java.lang.reflect.TypeVariable<?>[] typeVariables) {
        return NncUtils.map(typeVariables, store::fromTypeVariable);
    }

    private void buildClass(Class<?> mClass) {
        store.fromClass(mClass);
    }

    private List<IRAnnotation> getAnnotations(Annotation[] annotations) {
        return NncUtils.map(
                Arrays.asList(annotations),
                this::parseAnnotation
        );
    }

    private IRAnnotation parseAnnotation(Annotation annotation) {
        return new IRAnnotation(annotation.getClass());
    }

    private List<Modifier> getModifiers(int modifiers) {
        return Modifier.getByJavaModifiers(modifiers);
    }

    private IRPackage getPackage() {
        return store.getPackage(klass.getPackage().getName());
    }

    private IRClassKind getClassKind() {
        if(klass.isInterface()) {
            return IRClassKind.INTERFACE;
        }
        if(klass.isEnum()) {
            return IRClassKind.ENUM;
        }
        if(klass.isRecord()) {
            return IRClassKind.RECORD;
        }
        if(klass.isAnnotation()) {
            return IRClassKind.ANNOTATION;
        }
        else {
            return IRClassKind.CLASS;
        }
    }

}
