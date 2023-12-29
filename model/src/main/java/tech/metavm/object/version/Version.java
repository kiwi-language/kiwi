package tech.metavm.object.version;

import tech.metavm.entity.*;
import tech.metavm.object.version.rest.dto.VersionDTO;

import java.util.List;
import java.util.Set;

@EntityType("版本")
public class Version extends Entity {

    public static final IndexDef<Version> IDX_VERSION = IndexDef.createUnique(Version.class, "version");

    @EntityField("版本")
    private final long version;

    @ChildEntity("变更类型ID列表")
    private final ReadWriteArray<Long> changedTypeIds = addChild(new ReadWriteArray<>(Long.class), "changedTypeIds");

    @ChildEntity("删除类型ID列表")
    private final ReadWriteArray<Long> removedTypeIds = addChild(new ReadWriteArray<>(Long.class), "removedTypeIds");

    @ChildEntity("变更映射ID列表")
    private final ReadWriteArray<Long> changedMappingIds = addChild(new ReadWriteArray<>(Long.class), "changedMappingIds");

    @ChildEntity("删除映射ID列表")
    private final ReadWriteArray<Long> removedMappingIds = addChild(new ReadWriteArray<>(Long.class), "removedMappingIds");

    @ChildEntity("变更函数ID列表")
    private final ReadWriteArray<Long> changedFunctionIds = addChild(new ReadWriteArray<>(Long.class), "changedFunctionIds");

    @ChildEntity("删除函数ID列表")
    private final ReadWriteArray<Long> removedFunctionIds = addChild(new ReadWriteArray<>(Long.class), "removedFunctionIds");

    public Version(long version,
                   Set<Long> changedTypeIds,
                   Set<Long> removedTypeIds,
                   Set<Long> changedMappingIds,
                   Set<Long> removedMappingIds,
                   Set<Long> changedFunctionIds,
                   Set<Long> removedFunctionIds
    ) {
        this.version = version;
        this.changedTypeIds.addAll(changedTypeIds);
        this.removedTypeIds.addAll(removedTypeIds);
        this.changedMappingIds.addAll(changedMappingIds);
        this.removedMappingIds.addAll(removedMappingIds);
        this.changedFunctionIds.addAll(changedFunctionIds);
        this.removedFunctionIds.addAll(removedFunctionIds);
    }

    @Override
    public long getVersion() {
        return version;
    }

    public List<Long> getChangedTypeIds() {
        return changedTypeIds.toList();
    }

    public List<Long> getRemovedTypeIds() {
        return removedTypeIds.toList();
    }

    public List<Long> getChangedMappingIds() {
        return changedMappingIds.toList();
    }

    public List<Long> getRemovedMappingIds() {
        return removedMappingIds.toList();
    }

    public ReadWriteArray<Long> getChangedFunctionIds() {
        return changedFunctionIds;
    }

    public ReadWriteArray<Long> getRemovedFunctionIds() {
        return removedFunctionIds;
    }

    public VersionDTO toDTO() {
        return new VersionDTO(version, changedTypeIds, removedTypeIds);
    }

}
