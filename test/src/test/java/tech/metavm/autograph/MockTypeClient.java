package tech.metavm.autograph;

import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.rest.GetTreesRequest;
import tech.metavm.object.instance.rest.InstanceVersionDTO;
import tech.metavm.object.instance.rest.InstanceVersionsRequest;
import tech.metavm.object.instance.rest.TreeDTO;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.rest.dto.BatchSaveRequest;
import tech.metavm.object.type.rest.dto.TypeTreeQuery;
import tech.metavm.object.type.rest.dto.TypeTreeResponse;
import tech.metavm.system.BlockManager;
import tech.metavm.system.rest.dto.BlockDTO;
import tech.metavm.util.ContextUtil;

import java.util.List;

public class MockTypeClient implements TypeClient {

    private final TypeManager typeManager;
    private final BlockManager blockManager;
    private final InstanceManager instanceManager;

    public MockTypeClient(TypeManager typeManager, BlockManager blockManager, InstanceManager instanceManager) {
        this.typeManager = typeManager;
        this.blockManager = blockManager;
        this.instanceManager = instanceManager;
    }

    @Override
    public long getAppId() {
        return ContextUtil.getAppId();
    }

    @Override
    public void setAppId(long appId) {
        ContextUtil.setAppId(appId);
    }

    @Override
    public void batchSave(BatchSaveRequest request) {
        typeManager.batchSave(request);
    }

    @Override
    public void login(long appId, String loginName, String password) {
        ContextUtil.setAppId(appId);
    }

    @Override
    public BlockDTO getContainingBlock(long id) {
        return blockManager.getContaining(id);
    }

    @Override
    public List<BlockDTO> getActive(List<Long> typeIds) {
        return blockManager.getActive(typeIds);
    }

    @Override
    public List<InstanceVersionDTO> getVersions(InstanceVersionsRequest request) {
        return instanceManager.getVersions(request.ids());
    }

    @Override
    public List<TreeDTO> getTrees(GetTreesRequest request) {
        return instanceManager.getTrees(request.ids());
    }

    @Override
    public TypeTreeResponse queryTrees(TypeTreeQuery query) {
        return typeManager.queryTypeTrees(query);
    }
}
