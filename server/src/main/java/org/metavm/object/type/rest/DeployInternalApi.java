package org.metavm.object.type.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.metavm.object.type.TypeManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal-api/deploy")
public class DeployInternalApi {

    private final TypeManager typeManager;

    public DeployInternalApi(TypeManager typeManager) {
        this.typeManager = typeManager;
    }

    @SneakyThrows
    @PostMapping("/{appId}")
    public void deploy(HttpServletRequest servletRequest, @PathVariable("appId") long appId) {
        typeManager.deploy(appId, servletRequest.getInputStream());
    }

}
