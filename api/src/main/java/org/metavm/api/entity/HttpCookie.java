package org.metavm.api.entity;

import org.metavm.api.Entity;

@Entity(systemAPI = true, ephemeral = true)
public record HttpCookie(String name, String value) {

}
