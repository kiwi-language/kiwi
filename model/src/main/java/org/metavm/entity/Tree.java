package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.rest.TreeDTO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public record Tree(long id, long version, long nextNodeId, byte[] data) implements Comparable<Tree> {

    public static Tree fromDTO(TreeDTO treeDTO) {
        return new Tree(treeDTO.id(), treeDTO.version(), treeDTO.nextNodeId(), treeDTO.bytes());
    }

    public boolean migrated() {
        return data[0] == TreeTags.MIGRATED;
    }

    public InputStream openInput() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public int compareTo(@NotNull Tree o) {
        return Long.compare(id, o.id);
    }

    public TreeDTO toDTO() {
        return new TreeDTO(id, version, nextNodeId, data);
    }

}

