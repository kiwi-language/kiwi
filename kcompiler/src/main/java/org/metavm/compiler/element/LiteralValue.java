package org.metavm.compiler.element;

import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public record LiteralValue(Object value) implements Constant {

    public Type getType() {
        return switch (value) {
            case Byte ignored -> PrimitiveType.BYTE;
            case Short ignored -> PrimitiveType.SHORT;
            case Integer ignored -> PrimitiveType.INT;
            case Long ignored -> PrimitiveType.LONG;
            case Float ignored  -> PrimitiveType.FLOAT;
            case Double ignored -> PrimitiveType.DOUBLE;
            case String ignored -> PrimitiveType.STRING;
            case Character ignored -> PrimitiveType.CHAR;
            case Boolean ignored -> PrimitiveType.BOOLEAN;
            case null -> PrimitiveType.NULL;
            default -> throw new RuntimeException("Unexpected constant: " + value);
        };
    }

    @Override
    public void write(MvOutput output) {
        switch (value) {
            case Byte b -> {
                output.write(WireTypes.BYTE);
                output.write(b);
            }
            case Short s -> {
                output.write(WireTypes.SHORT);
                output.writeShort(s);
            }
            case Integer i -> {
                output.write(WireTypes.INT);
                output.writeInt(i);
            }
            case Long l -> {
                output.write(WireTypes.LONG);
                output.writeLong(l);
            }
            case Float f -> {
                output.write(WireTypes.FLOAT);
                output.writeFloat(f);
            }
            case Double d -> {
                output.write(WireTypes.DOUBLE);
                output.writeDouble(d);
            }
            case String str -> {
                output.write(WireTypes.STRING);
                output.writeUTF(str);
            }
            case Character c -> {
                output.write(WireTypes.CHAR);
                output.writeChar(c);
            }
            case Boolean b -> {
                output.write(WireTypes.INT);
                output.writeInt(b ? 1 : 0);
            }
            case null -> output.write(WireTypes.NULL);
            default -> throw new RuntimeException("Unexpected constant: " + value);
        }
    }
}
