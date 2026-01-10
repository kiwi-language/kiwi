package org.manul.entity.natives;

import org.manul.object.instance.core.InstanceRepository;

public record DefaultCallContext(
        InstanceRepository instanceRepository
) implements CallContext {
}
