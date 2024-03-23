package tech.metavm.object.instance.core;

import com.google.common.primitives.UnsignedBytes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.util.EncodingUtils;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

public abstract class Id implements Comparable<Id> {

    public abstract void write(InstanceOutput output);

    public static boolean isPersistedId(@Nullable String id) {
        return id != null && Id.parse(id).tryGetPhysicalId() != null;
    }

    public byte[] toBytes() {
        var bout = new ByteArrayOutputStream();
        write(new InstanceOutput(bout));
        return bout.toByteArray();
    }

    public String toString() {
        return EncodingUtils.bytesToHex(toBytes());
    }

    public static Id parse(String str) {
        return readId(new InstanceInput(new ByteArrayInputStream(EncodingUtils.hexToBytes(str))));
    }

    public static Id fromBytes(byte[] bytes) {
        return readId(new InstanceInput(new ByteArrayInputStream(bytes)));
    }

    public static Id readId(InstanceInput input) {
        var tag = IdTag.fromCode(input.read());
        return switch (tag) {
            case NULL -> new NullId();
            case DEFAULT_PHYSICAL -> new DefaultPhysicalId(input.readLong(), input.readLong(), readId(input));
            case CLASS_TYPE_PHYSICAL, ARRAY_TYPE_PHYSICAL, FIELD_PHYSICAL ->
                    new TaggedPhysicalId(tag, input.readLong(), input.readLong());
            case TMP -> new TmpId(input.readLong());
            case DEFAULT_VIEW -> new DefaultViewId(readId(input), readId(input));
            case CHILD_VIEW -> new ChildViewId(readId(input), readId(input), (ViewId) readId(input));
            case FIELD_VIEW ->
                    new FieldViewId((ViewId) readId(input), ViewId.readMappingId(input), readId(input),
                            PathViewId.readSourceId(input), readId(input));
            case ELEMENT_VIEW ->
                    new ElementViewId((ViewId) readId(input), ViewId.readMappingId(input), input.readInt(),
                            PathViewId.readSourceId(input), readId(input));
            case MOCK -> new MockId(input.readLong());
        };
    }

    public abstract Long tryGetPhysicalId();

    public long getPhysicalId() {
        return Objects.requireNonNull(tryGetPhysicalId());
    }

    public abstract boolean isTemporary();

    @Override
    public int compareTo(@NotNull Id o) {
        return UnsignedBytes.lexicographicalComparator().compare(toBytes(), o.toBytes());
    }

}
