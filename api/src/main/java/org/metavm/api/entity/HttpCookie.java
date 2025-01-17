package org.metavm.api.entity;

import org.metavm.api.Entity;

@Entity(systemAPI = true)
public interface HttpCookie {

    String name();

    String value();

}
