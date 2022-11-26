package tech.metavm.infra.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.dto.Result;
import tech.metavm.infra.BootstrapService;
import tech.metavm.infra.StdIdBockManager;
import tech.metavm.object.meta.StdTypeManager;

@RestController
@RequestMapping("/bootstrap")
public class BootstrapController {

    private final BootstrapService bootstrapService;

    public BootstrapController(BootstrapService bootstrapService) {
        this.bootstrapService = bootstrapService;
    }

    @PostMapping
    public Result<Void> boot() {
        bootstrapService.boot();
        return Result.success(null);
    }

}
