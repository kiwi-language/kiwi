package org.metavm.object.instance.core;

import com.google.common.primitives.UnsignedBytes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

public abstract class Id implements Comparable<Id> {

    public static @javax.annotation.Nullable Id tryParse(String s) {
        try {
            return parse(s);
        } catch (Exception ignored) {
            return null;
        }
    }

    public abstract void write(MvOutput output);

    public static boolean isPersistedId(@Nullable String id) {
        return id != null && Id.parse(id).tryGetTreeId() != null;
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
        try {
            return readId(new InstanceInput(new ByteArrayInputStream(EncodingUtils.hexToBytes(str))));
        }
        catch (Exception e) {
            throw new InternalException("Failed to parse id: " + str, e);
        }
    }

    public static Id fromBytes(byte[] bytes) {
        return readId(new InstanceInput(new ByteArrayInputStream(bytes)));
    }

    public static Id readId(MvInput input) {
        var maskedTagCode = input.read();
        var tag = IdTag.fromCode(maskedTagCode & 0x7F);
        var isArray = (maskedTagCode & 0x80) != 0;
        return switch (tag) {
            case NULL -> new NullId();
            case PHYSICAL -> new PhysicalId(isArray, input.readLong(), input.readLong());
            case TAGGED_PHYSICAL -> new TaggedPhysicalId(input.readLong(), input.readLong(), input.readInt());
            case TMP -> new TmpId(input.readLong());
            case MOCK -> new MockId(input.readLong());
        };
    }

    private final boolean isArray;

    protected Id(boolean isArray) {
        this.isArray = isArray;
    }

    public abstract Long tryGetTreeId();

    public long getTreeId() {
        return Objects.requireNonNull(tryGetTreeId());
    }

    public long getNodeId() {
        throw new UnsupportedOperationException();
    }

    public abstract boolean isTemporary();

    public boolean isArray() {
        return isArray;
    }

    public abstract int getTypeTag(TypeDefProvider typeDefProvider);

    @Override
    public int compareTo(@NotNull Id o) {
        return UnsignedBytes.lexicographicalComparator().compare(toBytes(), o.toBytes());
    }

    public boolean isRoot() {
        return false;
    }

    public Long tmpId() {
        return null;
    }

}
