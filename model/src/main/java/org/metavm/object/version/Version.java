package org.metavm.object.version;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IndexDef;
import org.metavm.entity.ReadWriteArray;
import org.metavm.object.version.rest.dto.VersionDTO;

import java.util.List;
import java.util.Set;

@EntityType
public class Version extends Entity {

    public static final IndexDef<Version> IDX_VERSION = IndexDef.createUnique(Version.class, "version");

    private final long version;

    @ChildEntity
    private final ReadWriteArray<String> changedTypeIds = addChild(new ReadWriteArray<>(String.class), "changedTypeIds");

    @ChildEntity
    private final ReadWriteArray<String> removedTypeIds = addChild(new ReadWriteArray<>(String.class), "removedTypeIds");

    @ChildEntity
    private final ReadWriteArray<String> changedMappingIds = addChild(new ReadWriteArray<>(String.class), "changedMappingIds");

    @ChildEntity
    private final ReadWriteArray<String> removedMappingIds = addChild(new ReadWriteArray<>(String.class), "removedMappingIds");

    @ChildEntity
    private final ReadWriteArray<String> changedFunctionIds = addChild(new ReadWriteArray<>(String.class), "changedFunctionIds");

    @ChildEntity
    private final ReadWriteArray<String> removedFunctionIds = addChild(new ReadWriteArray<>(String.class), "removedFunctionIds");

    public Version(long version,
                   Set<String> changedTypeIds,
                   Set<String> removedTypeIds,
                   Set<String> changedMappingIds,
                   Set<String> removedMappingIds,
                   Set<String> changedFunctionIds,
                   Set<String> removedFunctionIds
    ) {
        this.version = version;
        this.changedTypeIds.addAll(changedTypeIds);
        this.removedTypeIds.addAll(removedTypeIds);
        this.changedMappingIds.addAll(changedMappingIds);
        this.removedMappingIds.addAll(removedMappingIds);
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

    public List<String> getChangedMappingIds() {
        return changedMappingIds.toList();
    }

    public List<String> getRemovedMappingIds() {
        return removedMappingIds.toList();
    }

    public List<String> getChangedFunctionIds() {
        return changedFunctionIds.toList();
    }

    public List<String> getRemovedFunctionIds() {
        return removedFunctionIds.toList();
    }

    public VersionDTO toDTO() {
        return new VersionDTO(version, changedTypeIds.toList(), removedTypeIds.toList());
    }

}
