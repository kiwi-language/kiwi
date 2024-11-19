package org.metavm.autograph;

import org.metavm.object.instance.core.TreeVersion;
import org.metavm.object.instance.rest.GetTreesRequest;
import org.metavm.object.instance.rest.InstanceVersionsRequest;
import org.metavm.object.instance.rest.TreeDTO;
import org.metavm.object.type.rest.dto.TreeResponse;
import org.metavm.object.type.rest.dto.TypeTreeQuery;
import org.metavm.system.rest.dto.BlockDTO;

import java.util.List;

public interface TypeClient {

    long getAppId();

    void setAppId(long appId);

    void deploy(String mvaFile);

    void login(long appId, String loginName, String password);

    BlockDTO getContainingBlock(long id);

    List<BlockDTO> getActive(List<Long> typeIds);

    List<TreeVersion> getVersions(InstanceVersionsRequest request);

    List<TreeDTO> getTrees(GetTreesRequest request);

    TreeResponse queryTrees(TypeTreeQuery query);

}
