package tech.metavm.object.type.rest.dto;

import java.util.function.Consumer;

public class GenericElementDTO {
    private final String templateId;
    private final String id;

    public GenericElementDTO(String templateId, String id) {
        this.templateId = templateId;
        this.id = id;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getId() {
        return id;
    }

    public void forEachDescendant(Consumer<GenericElementDTO> action) {
        action.accept(this);
    }

}
