package org.metavm.meta.processor;

import lombok.Getter;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;

@Getter
public class EntityConfigException extends RuntimeException {

    private final @Nullable Element element;

    public EntityConfigException(String message, @Nullable Element element) {
        super(message);
        this.element = element;
    }

}
