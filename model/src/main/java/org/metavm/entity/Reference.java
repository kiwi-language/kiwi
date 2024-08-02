package org.metavm.entity;

import org.metavm.api.ValueObject;

public interface Reference extends ValueObject {

    Object resolve();

}
