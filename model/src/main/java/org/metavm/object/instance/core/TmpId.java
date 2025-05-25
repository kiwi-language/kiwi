package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;

import java.util.Objects;

@Slf4j
public final class TmpId extends Id {

    public static TmpId of(long tmpId) {
        return new TmpId(tmpId);
    }

    public static TmpId random() {
        return new TmpId(Utils.randomNonNegative());
    }

    public static String randomString() {
        return random().toString();
    }

    private final long tmpId;

    public TmpId(long tmpId) {
        super();
        this.tmpId = tmpId;
//        log.debug("Creating tmpId {}", this, new Exception());
    }

    @Override
    public void write(MvOutput output) {
        output.writeIdTag(IdTag.TMP, false);
        output.writeLong(tmpId);
    }

    @Override
    public Long tmpId() {
        return tmpId;
    }

    @Override
    public int getTag() {
        return 1;
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
    public int compareTo0(Id id) {
        return Long.compare(tmpId, ((TmpId) id).tmpId);
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
