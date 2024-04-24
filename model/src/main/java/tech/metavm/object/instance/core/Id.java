package tech.metavm.object.instance.core;

import com.google.common.primitives.UnsignedBytes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.object.type.rest.dto.TypeKey;
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
        var maskedTagCode = input.read();
        var tag = IdTag.fromCode(maskedTagCode & 0x7F);
        var isArray = (maskedTagCode & 0x80) != 0;
        return switch (tag) {
            case NULL -> new NullId();
            case OBJECT_PHYSICAL -> new DefaultPhysicalId(isArray, input.readLong(), input.readLong(), TypeKey.read(input));
            case CLASS_TYPE_PHYSICAL, ARRAY_TYPE_PHYSICAL, FIELD_PHYSICAL ->
                    new TaggedPhysicalId(tag, input.readLong(), input.readLong());
            case TMP -> new TmpId(input.readLong());
            case DEFAULT_VIEW -> new DefaultViewId(isArray, readId(input), readId(input));
            case CHILD_VIEW -> new ChildViewId(isArray, readId(input), readId(input), (ViewId) readId(input));
            case FIELD_VIEW ->
                    new FieldViewId(isArray, (ViewId) readId(input), ViewId.readMappingId(input), readId(input),
                            PathViewId.readSourceId(input), TypeKey.read(input));
            case ELEMENT_VIEW ->
                    new ElementViewId(isArray, (ViewId) readId(input), ViewId.readMappingId(input), input.readInt(),
                            PathViewId.readSourceId(input), TypeKey.read(input));
            case MOCK -> new MockId(input.readLong());
        };
    }

    private final boolean isArray;

    protected Id(boolean isArray) {
        this.isArray = isArray;
    }

    public abstract Long tryGetPhysicalId();

    public long getPhysicalId() {
        return Objects.requireNonNull(tryGetPhysicalId());
    }

    public abstract boolean isTemporary();

    public boolean isArray() {
        return isArray;
    }

    @Override
    public int compareTo(@NotNull Id o) {
        return UnsignedBytes.lexicographicalComparator().compare(toBytes(), o.toBytes());
    }

}
