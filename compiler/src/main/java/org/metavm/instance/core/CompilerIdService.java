package org.metavm.instance.core;

import org.metavm.autograph.TypeClient;
import org.metavm.object.type.Type;
import org.metavm.system.BaseIdService;

import java.util.List;
import java.util.Map;

public class CompilerIdService extends BaseIdService {

    public CompilerIdService(TypeClient typeClient) {
        super(new ServerBlockSource(typeClient));
    }

    @Override
    public Map<Type, List<Long>> allocate(long appId, Map<? extends Type, Integer> typeId2count) {
        throw new UnsupportedOperationException();
    }
}
