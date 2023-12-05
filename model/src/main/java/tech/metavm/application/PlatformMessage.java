package tech.metavm.application;

import tech.metavm.entity.Entity;

public class PlatformMessage extends Entity {

    private final String title;

    public PlatformMessage(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
