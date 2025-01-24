package org.metavm.classfile;

import lombok.extern.slf4j.Slf4j;
import org.metavm.flow.Code;
import org.metavm.flow.Lambda;
import org.metavm.flow.Method;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.DebugEnv;
import org.metavm.util.MvOutput;

@Slf4j
public class ClassFileWriter {

    private final MvOutput output;
    private final boolean tracing = DebugEnv.traceClassFileIO;

    public ClassFileWriter(MvOutput output) {
        this.output = output;
    }

    public void write(Klass klass) {
        writeKlass(klass);
    }

    private void writeKlass(Klass klass) {
        output.writeNullable(klass.getSourceTag(), output::writeInt);
        output.writeUTF(klass.getName());
        output.writeNullable(klass.getQualifiedName(), output::writeUTF);
        writeConstantPool(klass.getConstantPool());
        output.write(klass.getKind().code());
        output.writeInt(klass.getClassFlags());
        output.writeInt(klass.getSince());
        output.writeNullable(klass.getSuperTypeIndex(), output::writeInt);
        output.writeList(klass.getInterfaceIndexes(), output::writeInt);
        output.writeList(klass.getTypeParameters(), this::writeTypeVariable);
        output.writeList(klass.getFields(), this::writeField);
        output.writeList(klass.getStaticFields(), this::writeField);
        output.writeList(klass.getMethods(), this::writeMethod);
        output.writeList(klass.getIndices(), this::writeIndex);
        output.writeList(klass.getKlasses(), this::writeKlass);
        output.writeList(klass.getAttributes(), a -> a.write(output));
    }

    private void writeConstantPool(ConstantPool constantPool) {
        var values = constantPool.getEntries();
        output.writeInt(values.size());
        for (Value value : values) {
            writeConstant(value);
        }
    }

    private void writeConstant(Value value) {
        output.writeValue(value);
    }

    private void writeTypeVariable(TypeVariable typeVariable) {
        output.writeUTF(typeVariable.getName());
        output.writeList(typeVariable.getBoundIndexes(), output::writeInt);
        output.writeList(typeVariable.getAttributes(), a -> a.write(output));
    }

    private void writeField(Field field) {
        output.writeNullable(field.getSourceTag(), output::writeInt);
        output.writeUTF(field.getName());
        output.writeInt(field.getTypeIndex());
        output.write(field.getAccess().code());
        output.writeInt(field.getFlags());
        output.writeInt(field.getOrdinal());
        output.writeInt(field.getSince());
        output.writeNullable(field.getInitializerReference(), output::writeReference);
        output.writeBoolean(field.getState() == MetadataState.REMOVED);
    }

    private void writeMethod(Method method) {
        var tracing = this.tracing;
        if (tracing) log.trace("Writing method {}", method.getInternalName(null));
        output.writeUTF(method.getInternalName(null));
        output.writeUTF(method.getName());
        output.writeInt(method.getReturnTypeIndex());
        output.writeInt(method.getFlags());
        writeConstantPool(method.getConstantPool());
        output.writeList(method.getParameters(), this::writeParameter);
        output.writeList(method.getTypeParameters(), this::writeTypeVariable);
        output.writeList(method.getCapturedTypeVariables(), this::writeCapturedTypeVariable);
        output.writeList(method.getLambdas(), this::writeLambda);
        output.writeList(method.getKlasses(), this::writeKlass);
        output.writeList(method.getAttributes(), a -> a.write(output));
        if (method.hasBody()) writeCode(method.getCode());
    }

    private void writeLambda(Lambda lambda) {
        output.writeList(lambda.getParameters(), this::writeParameter);
        output.writeInt(lambda.getReturnTypeIndex());
        writeCode(lambda.getCode());
    }

    private void writeCapturedTypeVariable(CapturedTypeVariable capturedTypeVariable) {
        output.writeInt(capturedTypeVariable.getUncertainTypeIndex());
        output.writeReference(capturedTypeVariable.getTypeVariableReference());
        output.writeList(capturedTypeVariable.getAttributes(), a -> a.write(output));
    }

    private void writeParameter(Parameter parameter) {
        output.writeUTF(parameter.getName());
        output.writeInt(parameter.getTypeIndex());
        output.writeList(parameter.getAttributes(), a -> a.write(output));
    }

    private void writeIndex(Index index) {
        output.writeUTF(index.getName());
        output.writeBoolean(index.isUnique());
        output.writeNullable(index.getMethodReference(), output::writeReference);
        output.writeList(index.getFields(), this::writeIndexField);
    }

    private void writeIndexField(IndexField indexField) {
        output.writeUTF(indexField.getName());
        output.writeInt(indexField.getTypeIndex());
    }

    private void writeCode(Code code) {
        output.writeInt(code.getMaxLocals());
        output.writeInt(code.getMaxStack());
        output.writeBytes(code.getCode());
    }

}
