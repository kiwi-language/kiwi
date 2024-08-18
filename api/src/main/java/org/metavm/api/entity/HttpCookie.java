package org.metavm.api.entity;

import org.metavm.api.EntityType;

@EntityType(systemAPI = true, ephemeral = true)
public record HttpCookie(String name, String value) {

}
