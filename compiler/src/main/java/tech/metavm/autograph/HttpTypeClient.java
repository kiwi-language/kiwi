package tech.metavm.autograph;

import tech.metavm.object.instance.rest.GetTreesRequest;
import tech.metavm.object.instance.rest.InstanceVersionDTO;
import tech.metavm.object.instance.rest.InstanceVersionsRequest;
import tech.metavm.object.instance.rest.TreeDTO;
import tech.metavm.object.type.rest.dto.BatchSaveRequest;
import tech.metavm.object.type.rest.dto.TreeResponse;
import tech.metavm.object.type.rest.dto.TypeTreeQuery;
import tech.metavm.system.rest.dto.BlockDTO;
import tech.metavm.system.rest.dto.GetActiveBlocksRequest;
import tech.metavm.user.rest.dto.LoginInfo;
import tech.metavm.user.rest.dto.LoginRequest;
import tech.metavm.util.CompilerHttpUtils;
import tech.metavm.util.Constants;
import tech.metavm.util.TypeReference;

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
    public void batchSave(BatchSaveRequest request) {
        CompilerHttpUtils.post("/type/batch", request, new TypeReference<List<Long>>() {
        });
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
    public List<InstanceVersionDTO> getVersions(InstanceVersionsRequest request) {
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
