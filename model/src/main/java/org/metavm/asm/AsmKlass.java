package org.metavm.asm;

import org.metavm.flow.Method;
import org.metavm.object.type.*;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class AsmKlass implements AsmScope, AsmGenericDeclaration {
    @Nullable
    private final AsmScope parent;
    private final AsmCompilationUnit compilationUnit;
    private final Klass klass;
    private final List<String> typeParameterNames;
    final boolean isEnum;
    private AsmMethod classInitializer;
    @Nullable
    ClassType superType;
    int enumConstantOrdinal;
    final Set<Method> visitedMethods = new HashSet<>();
    final Set<Field> visitedFields = new HashSet<>();
    final Set<Klass> visitedInnerKlasses = new LinkedHashSet<>();

    AsmKlass(
            @Nullable AsmScope parent,
            AsmCompilationUnit compilationUnit, Klass klass,
            List<String> typeParameterNames,
            boolean isEnum
    ) {
        this.parent = parent;
        this.compilationUnit = compilationUnit;
        this.klass = klass;
        this.typeParameterNames = typeParameterNames;
        this.isEnum = isEnum;
    }

    public int nextEnumConstantOrdinal() {
        return enumConstantOrdinal++;
    }

    public String name() {
        return klass.getName();
    }

    public String rawName() {
        return klass.getName();
    }

    @Override
    public AsmCompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    @Nullable
    @Override
    public AsmScope parent() {
        return parent;
    }

    @Override
    public List<TypeVariable> getTypeParameters() {
        return klass.getTypeParameters();
    }

    @Override
    public Klass getGenericDeclaration() {
        return klass;
    }

    public Klass getKlass() {
        return klass;
    }

    @Override
    public String toString() {
        return "ClassInfo[" +
                "parent=" + parent + ", " +
                "klass=" + klass + ", " +
                "typeParameters=" + typeParameterNames + ']';
    }

    public AsmMethod getClassInitializer() {
        return classInitializer;
    }

    public void setClassInitializer(AsmMethod classInitializer) {
        this.classInitializer = classInitializer;
    }
}
