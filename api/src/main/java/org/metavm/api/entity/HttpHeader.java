package org.metavm.api.entity;

import org.metavm.api.Value;

@Value(systemAPI = true)
public interface HttpHeader {

    String name();

    String value();

}
