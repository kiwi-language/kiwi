package tech.metavm.flow;

import tech.metavm.dto.ErrorDTO;
import tech.metavm.entity.*;

@EntityType("错误")
public class Error extends Entity {

    @EntityField("元素")
    private final Element element;

    @EntityField("级别")
    private final ErrorLevel level;

    @EntityField("信息")
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
                    serContext.getRef(element),
                    message
            );
        }
    }

}
