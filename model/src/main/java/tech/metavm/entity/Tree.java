package tech.metavm.entity;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.rest.TreeDTO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public record Tree(Id id, long version, byte[] data) implements Comparable<Tree> {

    public static Tree fromDTO(TreeDTO treeDTO) {
        return new Tree(Id.parse(treeDTO.id()), treeDTO.version(), treeDTO.bytes());
    }

    public InputStream openInput() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public int compareTo(@NotNull Tree o) {
        return id.compareTo(o.id);
    }

    public TreeDTO toDTO() {
        return new TreeDTO(id.toString(), version, data);
    }

}

