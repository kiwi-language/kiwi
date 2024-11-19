package org.metavm.flow;

import org.metavm.object.type.ConstantPool;
import org.metavm.util.MvOutput;

import java.io.ByteArrayOutputStream;

public class CodeOutput extends MvOutput {

    private final ConstantPool constantPool;

    public CodeOutput(ConstantPool constantPool) {
        super(new ByteArrayOutputStream());
        this.constantPool = constantPool;
    }

    public void writeConstant(Object value) {
        writeShort(constantPool.addValue(value));
    }

    public byte[] toByteArray() {
        return ((ByteArrayOutputStream) getOut()).toByteArray();
    }

}
