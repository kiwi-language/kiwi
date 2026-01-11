package org.manul.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.manul.api.dto.SchemaResponse;
import org.manul.api.service.SchemaService;
import org.manul.context.http.Controller;
import org.manul.context.http.Get;
import org.manul.context.http.Mapping;
import org.manul.context.http.PathVariable;
import org.manul.util.ContextUtil;

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
