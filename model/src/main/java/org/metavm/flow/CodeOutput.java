package org.metavm.flow;

import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ConstantPool;
import org.metavm.util.MvOutput;

import java.io.ByteArrayOutputStream;

public class CodeOutput extends MvOutput {

    private final ConstantPool constantPool;

    public CodeOutput(ConstantPool constantPool) {
        super(new ByteArrayOutputStream());
        this.constantPool = constantPool;
    }

    public void writeConstant(Value value) {
        writeShort(constantPool.addValue(value));
    }

    public byte[] toByteArray() {
        return ((ByteArrayOutputStream) getOut()).toByteArray();
    }

}
