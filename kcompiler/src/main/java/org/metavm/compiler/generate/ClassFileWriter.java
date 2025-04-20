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
    private ConstPool constPool;

    public ClassFileWriter(OutputStream out) {
        output = new KlassOutput(out);
    }

    public void write(Clazz clazz) {
        writeClass(clazz);
    }

    private void writeClass(Clazz clazz) {
        if (Traces.traceWritingClassFile)
            log.trace("Writing class {}", clazz.getQualName());
        var prevCp = constPool;
        constPool = clazz.getConstPool();

        writeNullable(clazz.getSourceTag(), output::writeInt);
        writeUTF(clazz.getName());
        writeNullable(clazz.getQualName(), this::writeUTF);
        constPool.write(output);
        write(clazz.getClassTag().code());
        writeInt(clazz.getFlags());
        writeInt(clazz.getSince());
        writeNullable(clazz.getSuper(), this::writeConstant);
        writeList(clazz.getInterfaces().filter(ClassType::isInterface), this::writeConstant);
        writeList(clazz.getTypeParams(), this::writeTypeVariable);
        writeList(clazz.getFields().filter(f -> !f.isStatic()), this::writeField);
        var staticFields = clazz.getFields().filter(Field::isStatic);
        writeInt(clazz.getEnumConstants().size() + staticFields.size());
        clazz.getEnumConstants().forEach(this::writeEnumConstant);
        staticFields.forEach(this::writeField);
        writeList(clazz.getMethods(), this::writeMethod);
        writeList(clazz.getClasses(), this::writeClass);
        writeList(clazz.getAttributes(), a -> a.write(output));

        constPool = prevCp;
    }

    private void writeEnumConstant(EnumConst enumConst) {
        if (Traces.traceWritingClassFile) {
            log.trace("Writing enum constant {}. Initializer: {}",
                    enumConst.getQualifiedName(), enumConst.getInitializer());
        }
        writeNullable(enumConst.getSourceTag(), output::writeInt);
        writeUTF(enumConst.getName());
        writeConstant(enumConst.getType());
        write(Access.PUBLIC.code());
        writeInt(enumConst.getFlags());
        writeInt(enumConst.getOrdinal());
        writeInt(enumConst.getSince());
        writeNullable(enumConst.getInitializer(), m -> Elements.writeReference(m, output));
        output.writeBoolean(false);
    }

    private void writeMethod(Method method) {
        if (Traces.traceWritingClassFile)
            log.trace("Writing method {}({})", method.getLegacyName(), method.getParamTypes().map(Type::getTypeText).join(","));
        var tracing = ClassFileWriter.tracing;
        if (tracing) log.trace("Writing method {}", method.getInternalName(null));
        var prevCp = constPool;
        constPool = method.getConstPool();
        writeUTF(method.getInternalName(null));
        writeUTF(method.getLegacyName());
        writeConstant(method.getRetType());
        writeInt(method.getFlags());
        constPool.write(output);
        writeList(method.getParams(), this::writeParameter);
        writeList(method.getTypeParams(), this::writeTypeVariable);
        writeList(method.getCapturedTypeVariables(), this::writeCapturedType);
        writeList(method.getLambdas(), this::writeLambda);
        writeList(method.getClasses(), this::writeClass);
        writeList(method.getAttributes(), a -> a.write(output));
        if (method.hasBody()) writeCode(Objects.requireNonNull(method.getCode()));
        constPool = prevCp;
    }

    private void writeLambda(Lambda lambda) {
        writeUTF(lambda.getName());
        writeList(lambda.getParams(), this::writeParameter);
        writeConstant(lambda.getRetType());
        writeCode(lambda.getCode());
    }

    private void writeCapturedType(CapturedType capturedType) {
        writeUTF(capturedType.getName());
        writeConstant(capturedType.getUncertainType());
        Elements.writeReference(capturedType.getTypeVariable(), output);
        output.writeList(capturedType.getAttributes(), a -> a.write(output));
    }

    private void writeParameter(Param param) {
        writeUTF(param.getName());
        writeConstant(param.getType());
        writeList(param.getAttributes(), a -> a.write(output));
    }

    private void writeCode(Code code) {
        code.write(output);
    }

    private void writeTypeVariable(TypeVar typeVar) {
        writeUTF(typeVar.getName());
        writeInt(1);
        writeInt(constPool.put(typeVar.getBound()));
        writeList(typeVar.getAttributes(), a -> a.write(output));
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
        writeNullable(init, m -> Elements.writeReference(m, output));
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

    private void writeUTF(Name name) {
        output.writeUTF(name.toString());
    }

    private void writeUTF(String s) {
        output.writeUTF(s);
    }

    private void writeInt(int v) {
        output.writeInt(v);
    }

    private void writeConstant(Constant constant) {
        writeInt(constPool.put(constant));
    }

}
