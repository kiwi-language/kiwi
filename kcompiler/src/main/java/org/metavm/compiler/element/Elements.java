package org.metavm.compiler.element;

import org.metavm.object.type.SymbolRefs;
import org.metavm.util.DebugEnv;
import org.metavm.util.MvOutput;

import java.util.Objects;

public class Elements {

    public static void writeReference(Element element, MvOutput output) {
        var tracing = DebugEnv.traceClassFileIO;
        switch (element) {
            case Clazz klass -> {
                var scope = klass.getScope();
                if (scope instanceof Package) {
//                    if (klass.getSourceTag() == null) {
                        output.write(SymbolRefs.KLASS);
                        output.writeUTF(Objects.requireNonNull(klass.getQualifiedName()).toString());
//                    }
//                    else {
//                        buffer.write(SymbolRefs.KLASS_TAG);
//                        buffer.writeVarInt(klass.getSourceTag());
//                    }
                } else {
                    output.write(SymbolRefs.ENCLOSED_KLASS);
                    writeReference((Element) scope, output);
                    output.writeUTF(klass.getName().toString());
                }
            }
            case Method method -> {
                output.write(SymbolRefs.METHOD);
                writeReference(method.getDeclaringClass(), output);
                output.writeUTF(method.getInternalName(null));
            }
            case Field field -> {
//                if (field.getSourceTag() == null) {
                    output.write(field.isStatic() ? SymbolRefs.STATIC_FIELD : SymbolRefs.FIELD);
                    writeReference(field.getDeclaringClass(), output);
                    output.writeUTF(field.getName().toString());
//                } else {
//                    buffer.write(field.isStatic() ? SymbolRefs.STATIC_FIELD_TAG : SymbolRefs.FIELD_TAG);
//                    putRef(field.getDeclaringType().getReference());
//                    buffer.writeVarInt(field.getSourceTag());
//                }
            }
            case EnumConstant enumConstant -> {
                output.write(SymbolRefs.STATIC_FIELD);
                writeReference(enumConstant.getDeclaringClass(), output);
                output.writeUTF(enumConstant.getName().toString());
            }
            case FreeFunc function -> {
                output.write(SymbolRefs.FUNCTION);
                output.writeUTF(function.getName().toString());
            }
            case TypeVariable typeVariable -> {
                output.write(SymbolRefs.TYPE_VARIABLE);
                writeReference((Element) typeVariable.getGenericDeclaration(), output);
                output.writeUTF(typeVariable.getName().toString());
            }
//            case CapturedTypeVariable capturedTypeVariable -> {
//                buffer.write(SymbolRefs.CAPTURED_TYPE_VARIABLE);
//                putRef(((Instance) capturedTypeVariable.getScope()).getReference());
//                buffer.writeUTF(capturedTypeVariable.getName());
//            }
            case Lambda lambda -> {
                output.write(SymbolRefs.LAMBADA);
                writeReference(lambda.getFunction(), output);
                output.writeUTF(lambda.getName().toString());
            }
            case Parameter parameter -> {
                output.write(SymbolRefs.PARAMETER);
                writeReference(parameter.getExecutable(), output);
                output.writeUTF(parameter.getName().toString());
            }
            default -> throw new IllegalStateException("Unrecognized reference target: " + element);
        }
    }

}
