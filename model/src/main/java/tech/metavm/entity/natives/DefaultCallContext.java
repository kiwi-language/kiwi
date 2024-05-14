package tech.metavm.entity.natives;

import tech.metavm.object.instance.core.InstanceRepository;

public record DefaultCallContext(
        InstanceRepository instanceRepository
) implements CallContext {
}
