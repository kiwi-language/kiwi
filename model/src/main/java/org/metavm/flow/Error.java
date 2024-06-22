package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.common.rest.dto.ErrorDTO;
import org.metavm.entity.*;

@EntityType
public class Error extends Entity {

    private final Element element;

    private final ErrorLevel level;

    private final String message;

    public Error(Element element, ErrorLevel level, String message) {
        this.element = element;
        this.level = level;
        this.message = message;
    }

    public Element getElement() {
        return element;
    }

    public ErrorLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public ErrorDTO toDTO() {
        try(var serContext = SerializeContext.enter()) {
            return new ErrorDTO(
                    ElementKind.getByElementClass(element.getClass()).code(),
                    serContext.getStringId(element),
                    message
            );
        }
    }

}
