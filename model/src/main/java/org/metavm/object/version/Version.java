package org.metavm.object.version;

import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.entity.IndexDef;
import org.metavm.entity.ReadWriteArray;

import java.util.List;
import java.util.Set;

@Entity
public class Version extends org.metavm.entity.Entity {

    public static final IndexDef<Version> IDX_VERSION = IndexDef.createUnique(Version.class, "version");

    private final long version;

    @ChildEntity
    private final ReadWriteArray<String> changedTypeIds = addChild(new ReadWriteArray<>(String.class), "changedTypeIds");

    @ChildEntity
    private final ReadWriteArray<String> removedTypeIds = addChild(new ReadWriteArray<>(String.class), "removedTypeIds");

    @ChildEntity
    private final ReadWriteArray<String> changedFunctionIds = addChild(new ReadWriteArray<>(String.class), "changedFunctionIds");

    @ChildEntity
    private final ReadWriteArray<String> removedFunctionIds = addChild(new ReadWriteArray<>(String.class), "removedFunctionIds");

    public Version(long version,
                   Set<String> changedTypeIds,
                   Set<String> removedTypeIds,
                   Set<String> changedFunctionIds,
                   Set<String> removedFunctionIds
    ) {
        this.version = version;
        this.changedTypeIds.addAll(changedTypeIds);
        this.removedTypeIds.addAll(removedTypeIds);
        this.changedFunctionIds.addAll(changedFunctionIds);
        this.removedFunctionIds.addAll(removedFunctionIds);
    }

    public long getVersion() {
        return version;
    }

    public List<String> getChangedTypeIds() {
        return changedTypeIds.toList();
    }

    public List<String> getRemovedTypeIds() {
        return removedTypeIds.toList();
    }

    public List<String> getChangedFunctionIds() {
        return changedFunctionIds.toList();
    }

    public List<String> getRemovedFunctionIds() {
        return removedFunctionIds.toList();
    }

}
