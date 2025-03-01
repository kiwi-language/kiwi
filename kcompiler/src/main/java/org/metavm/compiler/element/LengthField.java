package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;

import java.util.function.Consumer;

@Slf4j
public class LengthField implements Variable, MemberRef {

    public static final LengthField instance = new LengthField();

    private LengthField() {}

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLengthField(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {

    }

    @Override
    public void write(ElementWriter writer) {
        writer.write("length");
    }

    @Override
    public SymName getName() {
        return SymNameTable.instance.length;
    }

    @Override
    public void setName(SymName name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getType() {
        return PrimitiveType.INT;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public void load(Code code) {
        code.arrayLength();
    }

    @Override
    public void store(Code code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invoke(Code code) {
        throw new UnsupportedOperationException();
    }
}
