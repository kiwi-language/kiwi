package org.metavm.object.type.rest;

import lombok.SneakyThrows;
import org.metavm.context.http.*;
import org.metavm.ddl.DeployService;
import org.metavm.object.type.TypeManager;

import java.io.InputStream;

@Controller
@Mapping("/internal-api/deploy")
public class DeployInternalApi {

    private final TypeManager typeManager;
    private final DeployService deployService;

    public DeployInternalApi(TypeManager typeManager, DeployService deployService) {
        this.typeManager = typeManager;
        this.deployService = deployService;
    }

    @SneakyThrows
    @Post("/{appId}")
    public String deploy(InputStream input, @PathVariable("appId") long appId,
                         @RequestParam(value = "no-backup", defaultValue = "false") boolean noBackup
    ) {
        return typeManager.deploy(appId, noBackup, input);
    }

    @Get("/status/{appId}/{deployId}")
    public String getDeployStatus(@PathVariable("appId") long appId, @PathVariable("deployId") String deployId) {
        return deployService.getDeployStatus(appId, deployId).name();
    }

    @Post("/revert/{appId}")
    public void revert(@PathVariable("appId") long appId) {
        deployService.revert(appId);
    }

    @Post("/abort/{appId}")
    public void abort(@PathVariable("appId") long appId) {
        deployService.abortDeployment(appId);
    }

}
