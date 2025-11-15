package org.metavm.compiler.element;

import org.metavm.compiler.type.ClassInst;

public abstract class AbstractElementVisitor<R> implements ElementVisitor<R> {

    @Override
    public R visitProject(Project project) {
        return visitElement(project);
    }

    @Override
    public R visitPackage(Package pkg) {
        return visitElement(pkg);
    }

    @Override
    public R visitClazz(Clazz clazz) {
        return visitElement(clazz);
    }

    @Override
    public R visitMethod(Method method) {
        return visitElement(method);
    }

    @Override
    public R visitField(Field field) {
        return visitElement(field);
    }

    @Override
    public R visitLambda(Lambda lambda) {
        return visitElement(lambda);
    }

    @Override
    public R visitParam(Param param) {
        return visitElement(param);
    }

    @Override
    public R visitLocalVariable(Variable variable) {
        return visitElement(variable);
    }

    @Override
    public R visitTypeVariable(TypeVar typeVar) {
        return visitElement(typeVar);
    }

    @Override
    public R visitFunction(FreeFunc function) {
        return visitElement(function);
    }

    @Override
    public R visitEnumConstant(EnumConst enumConst) {
        return visitElement(enumConst);
    }

    @Override
    public R visitFieldInst(FieldInst fieldInst) {
        return visitElement(fieldInst);
    }

    @Override
    public R visitMethodInst(MethodInst methodInst) {
        return visitElement(methodInst);
    }

    @Override
    public R visitBuiltinVariable(BuiltinVariable builtinVariable) {
        return visitElement(builtinVariable);
    }

    @Override
    public R visitFunctionInst(FreeFuncInst functionInst) {
        return visitElement(functionInst);
    }

    @Override
    public R visitClassInst(ClassInst classInst) {
        return visitElement(classInst);
    }

    @Override
    public R visitPartialMethodInst(PartialMethodInst partialMethodInst) {
        return visitElement(partialMethodInst);
    }

    @Override
    public R visitErrorElement(ErrorElement errorElement) {
        return visitElement(errorElement);
    }
}
