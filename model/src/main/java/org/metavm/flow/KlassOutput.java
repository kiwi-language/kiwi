package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.*;
import org.metavm.util.DebugEnv;
import org.metavm.util.MvOutput;

import java.io.OutputStream;
import java.util.Objects;

@Slf4j
public class KlassOutput extends MvOutput {


    public KlassOutput(OutputStream out) {
        super(out);
    }

    @Override
    public void writeReference(Reference reference) {
        var tracing = DebugEnv.traceClassFileIO;
        var entity = reference.resolveDurable();
        switch (entity) {
            case Klass klass -> {
                var scope = klass.getScope();
                if (scope == null) {
                    if (klass.getSourceTag() == null) {
                        write(SymbolRefs.KLASS);
                        writeUTF(Objects.requireNonNull(klass.getQualifiedName()));
                    }
                    else {
                        write(SymbolRefs.KLASS_TAG);
                        writeInt(klass.getSourceTag());
                    }
                } else {
                    write(SymbolRefs.ENCLOSED_KLASS);
                    writeReference(((Instance) scope).getReference());
                    writeUTF(klass.getName());
                }
            }
            case Method method -> {
                write(SymbolRefs.METHOD);
                writeReference(method.getDeclaringType().getReference());
                writeUTF(method.getInternalName(null));
            }
            case Field field -> {
                if (field.getSourceTag() == null) {
                    write(field.isStatic() ? SymbolRefs.STATIC_FIELD : SymbolRefs.FIELD);
                    writeReference(field.getDeclaringType().getReference());
                    writeUTF(field.getName());
                } else {
                    write(field.isStatic() ? SymbolRefs.STATIC_FIELD_TAG : SymbolRefs.FIELD_TAG);
                    writeReference(field.getDeclaringType().getReference());
                    writeInt(field.getSourceTag());
                }
            }
            case Index index -> {
                write(SymbolRefs.INDEX);
                writeReference(index.getDeclaringType().getReference());
                writeUTF(index.getName());
            }
            case Function function -> {
                write(SymbolRefs.FUNCTION);
                writeUTF(function.getName());
            }
            case TypeVariable typeVariable -> {
                write(SymbolRefs.TYPE_VARIABLE);
                writeReference(((Instance) typeVariable.getGenericDeclaration()).getReference());
                writeUTF(typeVariable.getName());
            }
            case CapturedTypeVariable capturedTypeVariable -> {
                write(SymbolRefs.CAPTURED_TYPE_VARIABLE);
                writeReference(((Instance) capturedTypeVariable.getScope()).getReference());
                writeUTF(capturedTypeVariable.getName());
            }
            case Lambda lambda -> {
                write(SymbolRefs.LAMBADA);
                writeReference(lambda.getFlow().getReference());
                writeUTF(lambda.getName());
            }
            case Parameter parameter -> {
                write(SymbolRefs.PARAMETER);
                writeReference(((Instance) parameter.getCallable()).getReference());
                writeUTF(parameter.getName());
            }
            default -> throw new IllegalStateException("Unrecognized reference target: " + entity);
        }
    }

}
