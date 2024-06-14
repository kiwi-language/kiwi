package org.metavm.application;

import org.metavm.entity.Entity;

public class PlatformMessage extends Entity {

    private final String title;

    public PlatformMessage(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
