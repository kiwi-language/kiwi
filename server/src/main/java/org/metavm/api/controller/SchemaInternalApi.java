package org.metavm.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.dto.SchemaResponse;
import org.metavm.api.service.SchemaService;
import org.metavm.util.ContextUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal-api/schema")
@Slf4j
public class SchemaInternalApi {

    private final SchemaService schemaService;

    public SchemaInternalApi(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @GetMapping("/{appId}")
    public SchemaResponse get(@PathVariable("appId") long appId) {
        log.debug("appId: {}", appId);
        ContextUtil.setAppId(appId);
        return schemaService.getSchema(appId);
    }

}
