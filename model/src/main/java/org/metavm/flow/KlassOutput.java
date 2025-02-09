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
                    write(SymbolRefs.KLASS);
                    writeUTF(Objects.requireNonNull(klass.getQualifiedName()));
                } else {
                    write(SymbolRefs.ENCLOSED_KLASS);
                    writeReference(((Instance) scope).getReference());
                    writeUTF(klass.getName());
                }
            }
            case Method method -> {
                write(SymbolRefs.METHOD);
                writeReference(method.getDeclaringType().getReference());
//                writeUTF(method.getInternalName(null));
                int index = method.getDeclaringType().getMethods().indexOf(method);
                if (tracing) log.trace("Writing method {} with index {}", method.getQualifiedName(), index);
                writeInt(index);
            }
            case Field field -> {
                write(SymbolRefs.FIELD);
                writeReference(field.getDeclaringType().getReference());
                writeUTF(field.getName());
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
                writeInt(capturedTypeVariable.getScope().getCapturedTypeVariables().indexOf(capturedTypeVariable));
            }
            case Lambda lambda -> {
                write(SymbolRefs.LAMBADA);
                writeReference(lambda.getFlow().getReference());
                writeInt(lambda.getFlow().getLambdas().indexOf(lambda));
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
