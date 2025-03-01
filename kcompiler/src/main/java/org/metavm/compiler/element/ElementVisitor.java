package org.metavm.compiler.element;

public interface ElementVisitor<R> {

    R visitElement(Element element);

    R visitProject(Project project);

    R visitPackage(Package pkg);

    R visitClazz(Clazz clazz);

    R visitMethod(Method method);

    R visitField(Field field);

    R visitLambda(Lambda lambda);

    R visitParameter(Parameter parameter);

    R visitLocalVariable(Variable variable);

    R visitTypeVariable(TypeVariable typeVariable);

    R visitFunction(FreeFunc function);

    R visitEnumConstant(EnumConstant enumConstant);

    R visitFieldInst(FieldInst fieldInst);

    R visitMethodInst(MethodInst methodInst);

    R visitBuiltinVariable(BuiltinVariable builtinVariable);

    R visitFunctionInst(FreeFuncInst functionInst);

    R visitLengthField(LengthField lengthField);
}
