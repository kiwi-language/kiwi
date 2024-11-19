package org.metavm.autograph;

import org.metavm.object.instance.core.TreeVersion;
import org.metavm.object.instance.rest.GetTreesRequest;
import org.metavm.object.instance.rest.InstanceVersionsRequest;
import org.metavm.object.instance.rest.TreeDTO;
import org.metavm.object.type.rest.dto.TreeResponse;
import org.metavm.object.type.rest.dto.TypeTreeQuery;
import org.metavm.system.rest.dto.BlockDTO;
import org.metavm.system.rest.dto.GetActiveBlocksRequest;
import org.metavm.user.rest.dto.LoginInfo;
import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.CompilerHttpUtils;
import org.metavm.util.Constants;
import org.metavm.util.TypeReference;

import java.util.List;

public class HttpTypeClient implements TypeClient {

    @Override
    public long getAppId() {
        return CompilerHttpUtils.getAppId();
    }

    @Override
    public void setAppId(long appId) {
        CompilerHttpUtils.setAppId(appId);
    }

    @Override
    public void deploy(String mvaPath) {
        CompilerHttpUtils.upload("/type/deploy", mvaPath, new TypeReference<Void>() {});
    }

    @Override
    public void login(long appId, String loginName, String password) {
        CompilerHttpUtils.setAppId(2L);
        CompilerHttpUtils.post("/login", new LoginRequest(Constants.PLATFORM_APP_ID, loginName, password),
                new TypeReference<LoginInfo>() {
                });
        CompilerHttpUtils.post("/platform-user/enter-app/" + appId, null, new TypeReference<LoginInfo>() {
        });
        CompilerHttpUtils.setAppId(appId);
    }

    @Override
    public BlockDTO getContainingBlock(long id) {
        return CompilerHttpUtils.get("/block/containing/" + id, new TypeReference<>() {
        });
    }

    @Override
    public List<BlockDTO> getActive(List<Long> typeIds) {
        return CompilerHttpUtils.post("/block/active",
                new GetActiveBlocksRequest(typeIds), new TypeReference<>() {
                });
    }

    @Override
    public List<TreeVersion> getVersions(InstanceVersionsRequest request) {
        return CompilerHttpUtils.post("/instance/versions", request, new TypeReference<>() {});
    }

    @Override
    public List<TreeDTO> getTrees(GetTreesRequest request) {
        return CompilerHttpUtils.post("/instance/trees",
                request, new TypeReference<>() {});
    }

    @Override
    public TreeResponse queryTrees(TypeTreeQuery query) {
        return CompilerHttpUtils.post(
                "/type/query-trees",
                query,
                new TypeReference<>() {
                }
        );
    }


}
