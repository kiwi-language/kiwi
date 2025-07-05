package org.metavm.object.type.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.metavm.common.Result;
import org.metavm.object.type.TypeManager;
import org.metavm.object.type.rest.dto.GetSourceTagRequest;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/type")
public class TypeController {

    private final TypeManager typeManager;

    public TypeController(TypeManager typeManager) {
        this.typeManager = typeManager;
    }

    @PostMapping("/deploy/{appId}")
    public Result<Void> deploy(HttpServletRequest servletRequest, @PathVariable("appId") long appId) throws IOException {
        typeManager.ensureAppAccess(appId);
        typeManager.deploy(appId, servletRequest.getInputStream());
        return Result.voidSuccess();
    }

    @PostMapping("/source-tag")
    public Result<Integer> getSourceTag(@RequestBody GetSourceTagRequest request) {
        return Result.success(typeManager.getSourceTag(request.appId(), request.name()));
    }

}
