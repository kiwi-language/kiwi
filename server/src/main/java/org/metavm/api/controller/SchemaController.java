package org.metavm.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.metavm.api.dto.SchemaResponse;
import org.metavm.api.service.SchemaService;
import org.metavm.common.ErrorCode;
import org.metavm.common.Result;
import org.metavm.util.BusinessException;
import org.metavm.util.ContextUtil;
import org.metavm.util.Headers;
import org.metavm.util.ValueUtils;
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
    public Result<SchemaResponse> get(HttpServletRequest servletRequest) {
        verify(servletRequest);
        return Result.success(schemaService.getSchema());
    }

    private void verify(HttpServletRequest request) {
        var appIdStr = request.getHeader(Headers.APP_ID);
        if (appIdStr == null || !ValueUtils.isIntegerStr(appIdStr))
            throw new BusinessException(ErrorCode.AUTH_FAILED);
        var appId = Long.parseLong(appIdStr);
        ContextUtil.setAppId(appId);
    }


}
