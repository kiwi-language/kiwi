package tech.metavm.object.instance.core;

import tech.metavm.util.EncodingUtils;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public abstract class Id {

    public abstract void write(InstanceOutput output);

    public String toString() {
        var bout = new ByteArrayOutputStream();
        write(new InstanceOutput(bout));
        return EncodingUtils.bytesToHex(bout.toByteArray());
    }

    public static Id parse(String str) {
        return readId(new InstanceInput(new ByteArrayInputStream(EncodingUtils.hexToBytes(str))));
    }


    public static Id readId(InstanceInput input) {
        var tag = input.read();
        return switch (tag) {
            case PhysicalId.TAG -> new PhysicalId(input.readLong());
            case TmpId.TAG -> new TmpId(input.readLong());
            case ViewId.TAG -> new ViewId(input.readLong(), readId(input));
            case ChildViewId.TAG -> new ChildViewId(input.readLong(), readId(input), (ViewId) readId(input));
            default -> throw new IllegalArgumentException("Unknown instance id tag: " + tag);
        };
    }

    public abstract Long tryGetPhysicalId();

    public abstract boolean isTemporary();

}
