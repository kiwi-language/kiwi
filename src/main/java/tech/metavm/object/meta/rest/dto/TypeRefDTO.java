package tech.metavm.object.meta.rest.dto;

public class TypeRefDTO {
    private final int category;
    private final long id;
    private final String name;

    public TypeRefDTO(int category, long id, String name) {
        this.category = category;
        this.id = id;
        this.name = name;
    }

    public int getCategory() {
        return category;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
