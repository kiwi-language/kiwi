package org.metavm.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.dto.SchemaResponse;
import org.metavm.api.service.SchemaService;
import org.metavm.context.http.Controller;
import org.metavm.context.http.Get;
import org.metavm.context.http.Mapping;
import org.metavm.context.http.PathVariable;
import org.metavm.util.ContextUtil;

@Controller
@Mapping("/internal-api/schema")
@Slf4j
public class SchemaInternalApi {

    private final SchemaService schemaService;

    public SchemaInternalApi(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @Get("/{appId}")
    public SchemaResponse get(@PathVariable("appId") long appId) {
        ContextUtil.setAppId(appId);
        return schemaService.getSchema(appId);
    }

}
