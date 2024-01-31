package tech.metavm.instance.core;

import tech.metavm.autograph.TypeClient;
import tech.metavm.system.BaseIdService;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Map;

public class CompilerIdService extends BaseIdService {

    public CompilerIdService(TypeClient typeClient) {
        super(new ServerBlockSource(typeClient));
    }

    @Override
    public Map<Type, List<Long>> allocate(long appId, Map<Type, Integer> typeId2count) {
        throw new UnsupportedOperationException();
    }
}
