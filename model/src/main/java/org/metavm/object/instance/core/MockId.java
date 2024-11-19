package org.metavm.object.instance.core;

import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;

import java.util.Objects;

public class MockId extends Id {

    private final long id;

    public MockId(long id) {
        super(false);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public void write(MvOutput output) {
        output.writeIdTag(IdTag.MOCK, false);
        output.writeLong(id);
    }

    @Override
    public boolean equals(Object entity) {
        if (this == entity) return true;
        if (!(entity instanceof MockId mockId)) return false;
        return id == mockId.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
}
