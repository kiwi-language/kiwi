package tech.metavm.instance.core;

import tech.metavm.system.BaseIdService;
import tech.metavm.system.BlockSource;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Map;

public class CompilerIdService extends BaseIdService {

    public CompilerIdService() {
        super(new ServerBlockSource());
    }

    @Override
    public Map<Type, List<Long>> allocate(long appId, Map<Type, Integer> typeId2count) {
        throw new UnsupportedOperationException();
    }
}
