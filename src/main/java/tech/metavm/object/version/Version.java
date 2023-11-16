package tech.metavm.object.version;

import tech.metavm.entity.*;
import tech.metavm.object.version.rest.dto.VersionDTO;

import java.util.List;
import java.util.Set;

@EntityType("版本")
public class Version extends Entity {

    public static final IndexDef<Version> IDX_VERSION = IndexDef.uniqueKey(Version.class, "version");

    @EntityField("版本")
    private final long version;

    @ChildEntity("变更类型ID列表")
    private final ReadWriteArray<Long> changeTypeIds = addChild(new ReadWriteArray<>(Long.class), "changeTypeIds");

    @ChildEntity("删除类型ID列表")
    private final ReadWriteArray<Long> removedTypeIds = addChild(new ReadWriteArray<>(Long.class), "changeTypeIds");

    public Version(long version, Set<Long> changedTypeIds, Set<Long> removedTypeIds) {
        this.version = version;
        this.changeTypeIds.addAll(changedTypeIds);
        this.removedTypeIds.addAll(removedTypeIds);
    }

    @Override
    public long getVersion() {
        return version;
    }

    public List<Long> getChangeTypeIds() {
        return changeTypeIds.toList();
    }

    public List<Long> getRemovedTypeIds() {
        return removedTypeIds.toList();
    }

    public VersionDTO toDTO() {
        return new VersionDTO(version, changeTypeIds, removedTypeIds);
    }

}
