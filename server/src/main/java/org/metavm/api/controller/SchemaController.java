package org.metavm.api.controller;

import org.metavm.api.dto.SchemaResponse;
import org.metavm.api.service.SchemaService;
import org.metavm.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schema")
public class SchemaController {

    private final SchemaService schemaService;

    public SchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @GetMapping
    public Result<SchemaResponse> get() {
        return Result.success(schemaService.getSchema());
    }


}
