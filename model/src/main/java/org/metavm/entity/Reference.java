package org.metavm.entity;

import org.metavm.api.Value;

public interface Reference extends Value {

    Object resolve();

}
