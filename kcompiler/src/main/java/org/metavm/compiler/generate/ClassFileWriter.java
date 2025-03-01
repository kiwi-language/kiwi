package org.metavm.compiler.generate;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.*;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;
import org.metavm.flow.KlassOutput;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public class ClassFileWriter {

    private static final boolean tracing = false;

    private final MvOutput output;
    private ConstantPool constantPool;

    public ClassFileWriter(OutputStream out) {
        output = new KlassOutput(out);
    }

    public void write(Clazz clazz) {
        writeClass(clazz);
    }

    private void writeClass(Clazz clazz) {
        if (Traces.traceWritingClassFile)
            log.trace("Writing class {}", clazz.getQualifiedName());
        var prevCp = constantPool;
        constantPool = clazz.getConstantPool();

        writeNullable(clazz.getSourceTag(), output::writeInt);
        writeUTF(clazz.getName());
        writeNullable(clazz.getQualifiedName(), this::writeUTF);
        constantPool.write(output);
        write(clazz.getTag().code());
        writeInt(clazz.getFlags());
        writeInt(clazz.getSince());
        writeNullable(clazz.getSuper(), this::writeConstant);
        writeList(clazz.getInterfaces().filter(ClassType::isInterface), this::writeConstant);
        writeList(clazz.getTypeParameters(), this::writeTypeVariable);
        writeList(clazz.getFields().filter(f -> !f.isStatic()), this::writeField);
        var staticFields = clazz.getFields().filter(Field::isStatic);
        writeInt(clazz.getEnumConstants().size() + staticFields.size());
        clazz.getEnumConstants().forEach(this::writeEnumConstant);
        staticFields.forEach(this::writeField);
        writeList(clazz.getMethods(), this::writeMethod);
        writeList(clazz.getClasses(), this::writeClass);
        writeList(clazz.getAttributes(), a -> a.write(output));

        constantPool = prevCp;
    }

    private void writeEnumConstant(EnumConstant enumConstant) {
        if (Traces.traceWritingClassFile) {
            log.trace("Writing enum constant {}. Initializer: {}",
                    enumConstant.getQualifiedName(), enumConstant.getInitializer());
        }
        writeNullable(enumConstant.getSourceTag(), output::writeInt);
        writeUTF(enumConstant.getName());
        writeConstant(enumConstant.getType());
        write(Access.PUBLIC.code());
        writeInt(enumConstant.getFlags());
        writeInt(enumConstant.getOrdinal());
        writeInt(enumConstant.getSince());
        writeNullable(enumConstant.getInitializer(), m -> Elements.writeReference(m, output));
        output.writeBoolean(false);
    }

    private void writeMethod(Method method) {
        if (Traces.traceWritingClassFile)
            log.trace("Writing method {}({})", method.getLegacyName(), method.getParameterTypes().map(Type::getText).join(","));
        var tracing = ClassFileWriter.tracing;
        if (tracing) log.trace("Writing method {}", method.getInternalName(null));
        var prevCp = constantPool;
        constantPool = method.getConstantPool();
        writeUTF(method.getInternalName(null));
        writeUTF(method.getLegacyName());
        writeConstant(method.getReturnType());
        writeInt(method.getFlags());
        constantPool.write(output);
        writeList(method.getParameters(), this::writeParameter);
        writeList(method.getTypeParameters(), this::writeTypeVariable);
        writeList(method.getCapturedTypeVariables(), this::writeCapturedType);
        writeList(method.getLambdas(), this::writeLambda);
        writeList(method.getClasses(), this::writeClass);
        writeList(method.getAttributes(), a -> a.write(output));
        if (method.hasBody()) writeCode(Objects.requireNonNull(method.getCode()));
        constantPool = prevCp;
    }

    private void writeLambda(Lambda lambda) {
        writeUTF(lambda.getName());
        writeList(lambda.getParameters(), this::writeParameter);
        writeConstant(lambda.getReturnType());
        writeCode(lambda.getCode());
    }

    private void writeCapturedType(CapturedType capturedType) {
        writeUTF(capturedType.getName());
        writeConstant(capturedType.getUncertainType());
        Elements.writeReference(capturedType.getTypeVariable(), output);
        output.writeList(capturedType.getAttributes(), a -> a.write(output));
    }

    private void writeParameter(Parameter parameter) {
        writeUTF(parameter.getName());
        writeConstant(parameter.getType());
        writeList(parameter.getAttributes(), a -> a.write(output));
    }

    private void writeCode(Code code) {
        code.write(output);
    }

    private void writeTypeVariable(TypeVariable typeVariable) {
        writeUTF(typeVariable.getName());
        writeInt(1);
        writeInt(constantPool.put(typeVariable.getBound()));
        writeList(typeVariable.getAttributes(), a -> a.write(output));
    }

    private void writeField(Field field) {
        writeNullable(field.getSourceTag(), output::writeInt);
        writeUTF(field.getName());
        writeConstant(field.getType());
        write(field.getAccess().code());
        writeInt(field.getFlags());
        writeInt(field.getOrdinal());
        writeInt(field.getSince());
        var init = field.getInitializer();
        writeNullable(init, m -> Elements.writeReference(m.getFunction(), output));
        output.writeBoolean(field.isDeleted());
    }

    private void write(int b) {
        output.write(b);
    }

    private <E> void writeNullable(@Nullable E value, Consumer<E> action) {
        output.writeNullable(value, action);
    }

    private <E> void writeList(List<E> list, Consumer<E> action) {
        output.writeList(list, action);
    }

    private void writeUTF(SymName name) {
        output.writeUTF(name.toString());
    }

    private void writeUTF(String s) {
        output.writeUTF(s);
    }

    private void writeInt(int v) {
        output.writeInt(v);
    }

    private void writeConstant(Constant constant) {
        writeInt(constantPool.put(constant));
    }

}
