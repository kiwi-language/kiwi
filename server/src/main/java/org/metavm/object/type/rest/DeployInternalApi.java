package org.metavm.object.type.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.metavm.ddl.DeployService;
import org.metavm.object.type.TypeManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public void deploy(HttpServletRequest servletRequest, @PathVariable("appId") long appId) {
        typeManager.deploy(appId, servletRequest.getInputStream());
    }

    @PostMapping("/revert/{appId}")
    public void revert(@PathVariable("appId") long appId) {
        deployService.revert(appId);
    }

}
