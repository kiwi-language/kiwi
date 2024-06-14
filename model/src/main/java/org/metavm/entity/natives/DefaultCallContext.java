package org.metavm.entity.natives;

import org.metavm.object.instance.core.InstanceRepository;

public record DefaultCallContext(
        InstanceRepository instanceRepository
) implements CallContext {
}
