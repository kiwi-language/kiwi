package tech.metavm.entity;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.rest.TreeDTO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public record Tree(long id, long version, byte[] data) implements Comparable<Tree> {


    public Tree {
        if(id == 1231904639L)
            System.out.println("Caught");
    }

    public static Tree fromDTO(TreeDTO treeDTO) {
        return new Tree(treeDTO.id(), treeDTO.version(), treeDTO.bytes());
    }

    public InputStream openInput() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public int compareTo(@NotNull Tree o) {
        return Long.compare(id, o.id);
    }

    public TreeDTO toDTO() {
        return new TreeDTO(id, version, data);
    }

}

