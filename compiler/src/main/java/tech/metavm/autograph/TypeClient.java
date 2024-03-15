package tech.metavm.autograph;

import tech.metavm.object.instance.rest.GetTreesRequest;
import tech.metavm.object.instance.rest.InstanceVersionDTO;
import tech.metavm.object.instance.rest.InstanceVersionsRequest;
import tech.metavm.object.instance.rest.TreeDTO;
import tech.metavm.object.type.rest.dto.BatchSaveRequest;
import tech.metavm.object.type.rest.dto.TypeTreeQuery;
import tech.metavm.object.type.rest.dto.TreeResponse;
import tech.metavm.system.rest.dto.BlockDTO;

import java.util.List;

public interface TypeClient {

    String getAppId();

    void setAppId(String appId);

    void batchSave(BatchSaveRequest request);

    void login(String appId, String loginName, String password);

    BlockDTO getContainingBlock(long id);

    List<BlockDTO> getActive(List<Long> typeIds);

    List<InstanceVersionDTO> getVersions(InstanceVersionsRequest request);

    List<TreeDTO> getTrees(GetTreesRequest request);

    TreeResponse queryTrees(TypeTreeQuery query);

}
