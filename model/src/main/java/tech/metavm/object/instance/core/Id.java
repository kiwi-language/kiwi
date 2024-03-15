package tech.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import tech.metavm.util.EncodingUtils;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

public abstract class Id implements Comparable<Id> {

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
            case PhysicalId.TAG -> input.readId();
            case TmpId.TAG -> new TmpId(input.readLong());
            case DefaultViewId.TAG -> new DefaultViewId(readId(input), readId(input));
            case ChildViewId.TAG -> new ChildViewId(readId(input), readId(input), (ViewId) readId(input));
            case FieldViewId.TAG ->
                    new FieldViewId((ViewId) readId(input), readId(input), readId(input),
                            PathViewId.readSourceId(input), readId(input));
            case ElementViewId.TAG ->
                    new ElementViewId((ViewId) readId(input), readId(input), input.readInt(),
                            PathViewId.readSourceId(input), readId(input));
            default -> throw new IllegalArgumentException("Unknown instance id tag: " + tag);
        };
    }

    public abstract Long tryGetPhysicalId();

    public long getPhysicalId() {
        return Objects.requireNonNull(tryGetPhysicalId());
    }

    public abstract boolean isTemporary();

    @Override
    public int compareTo(@NotNull Id o) {
        return Long.compare(NncUtils.orElse(tryGetPhysicalId(), 0L), NncUtils.orElse(o.tryGetPhysicalId(), 0L));
    }

}
