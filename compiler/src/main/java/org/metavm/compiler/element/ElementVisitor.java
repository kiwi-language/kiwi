package org.metavm.compiler.element;

import org.metavm.compiler.type.ClassInst;

public interface ElementVisitor<R> {

    R visitElement(Element element);

    R visitProject(Project project);

    R visitPackage(Package pkg);

    R visitClazz(Clazz clazz);

    R visitMethod(Method method);

    R visitField(Field field);

    R visitLambda(Lambda lambda);

    R visitParam(Param param);

    R visitLocalVariable(Variable variable);

    R visitTypeVariable(TypeVar typeVar);

    R visitFunction(FreeFunc function);

    R visitEnumConstant(EnumConst enumConst);

    R visitFieldInst(FieldInst fieldInst);

    R visitMethodInst(MethodInst methodInst);

    R visitBuiltinVariable(BuiltinVariable builtinVariable);

    R visitFunctionInst(FreeFuncInst functionInst);

    R visitClassInst(ClassInst classInst);

    R visitPartialMethodInst(PartialMethodInst partialMethodInst);

    R visitErrorElement(ErrorElement errorElement);
}
