package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.InstanceOutput;
import org.metavm.util.NncUtils;

import java.util.Objects;

@Slf4j
public final class TmpId extends Id {

    public static TmpId of(long tmpId) {
        return new TmpId(tmpId);
    }

    public static TmpId random() {
        return new TmpId(NncUtils.randomNonNegative());
    }

    public static String randomString() {
        return random().toString();
    }

    private final long tmpId;

    public TmpId(long tmpId) {
        super(false);
        this.tmpId = tmpId;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.TMP, false);
        output.writeLong(tmpId);
    }

    public long getTmpId() {
        return tmpId;
    }

    @Override
    public Long tryGetTreeId() {
        return null;
    }

    @Override
    public boolean isTemporary() {
        return true;
    }

    @Override
    public int getTypeTag(TypeDefProvider typeDefProvider) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof TmpId tmpId1)) return false;
        return tmpId == tmpId1.tmpId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tmpId);
    }
}
