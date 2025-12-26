package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.type.Types;
import org.metavm.entity.SymbolRefs;
import org.metavm.util.DebugEnv;
import org.metavm.compiler.generate.KlassOutput;

import java.util.Objects;

@Slf4j
public class Elements {

    public static void writeReference(Element element, KlassOutput output) {
        var tracing = DebugEnv.traceClassFileIO;
        switch (element) {
            case Clazz klass -> {
                if (klass == Types.instance.getStringClass()) {
                    output.write(SymbolRefs.KLASS);
                    output.writeUTF("java.lang.String");
                    return;
                }
                var scope = klass.getScope();
                if (scope instanceof Package) {
//                    if (klass.getSourceTag() == null) {
                        output.write(SymbolRefs.KLASS);
                        output.writeUTF(Objects.requireNonNull(klass.getQualName()).toString());
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
                writeReference(method.getDeclClass(), output);
                output.writeUTF(method.getInternalName(null));
            }
            case Field field -> {
//                if (field.getSourceTag() == null) {
                    output.write(field.isStatic() ? SymbolRefs.STATIC_FIELD : SymbolRefs.FIELD);
                    writeReference(field.getDeclClass(), output);
                    output.writeUTF(field.getName().toString());
//                } else {
//                    buffer.write(field.isStatic() ? SymbolRefs.STATIC_FIELD_TAG : SymbolRefs.FIELD_TAG);
//                    putRef(field.getDeclaringType().getReference());
//                    buffer.writeVarInt(field.getSourceTag());
//                }
            }
            case FreeFunc function -> {
                output.write(SymbolRefs.FUNCTION);
                output.writeUTF(function.getName().toString());
            }
            case TypeVar typeVar -> {
                output.write(SymbolRefs.TYPE_VARIABLE);
                writeReference((Element) typeVar.getGenericDeclaration(), output);
                output.writeUTF(typeVar.getName().toString());
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
            case Param param -> {
                output.write(SymbolRefs.PARAMETER);
                writeReference(param.getExecutable(), output);
                output.writeUTF(param.getName().toString());
            }
            default -> throw new IllegalStateException("Unrecognized reference target: " + element);
        }
    }

}
