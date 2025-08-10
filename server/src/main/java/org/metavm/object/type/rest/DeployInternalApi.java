package org.metavm.object.type.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.metavm.ddl.DeployService;
import org.metavm.object.type.TypeManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal-api/deploy")
public class DeployInternalApi {

    private final TypeManager typeManager;
    private final DeployService deployService;

    public DeployInternalApi(TypeManager typeManager, DeployService deployService) {
        this.typeManager = typeManager;
        this.deployService = deployService;
    }

    @SneakyThrows
    @PostMapping("/{appId}")
    public String deploy(HttpServletRequest servletRequest, @PathVariable("appId") long appId) {
        return typeManager.deploy(appId, servletRequest.getInputStream());
    }

    @GetMapping("/status/{appId}/{deployId}")
    public String getDeployStatus(@PathVariable("appId") long appId, @PathVariable("deployId") String deployId) {
        return deployService.getDeployStatus(appId, deployId).name();
    }

    @PostMapping("/revert/{appId}")
    public void revert(@PathVariable("appId") long appId) {
        deployService.revert(appId);
    }

}
