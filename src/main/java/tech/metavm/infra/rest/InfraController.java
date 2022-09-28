package tech.metavm.infra.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.dto.Result;
import tech.metavm.infra.IdService;

@RestController
@RequestMapping("/infra")
public class InfraController {

    @Autowired
    private IdService idService;

    @PostMapping("/init-id-bulks")
    public Result<Void> initIdBulks(@RequestParam(value = "initId", defaultValue = "0") long initId) {
        idService.initBulks(initId);
        return Result.success(null);
    }

}
