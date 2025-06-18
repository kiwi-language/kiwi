package org.metavm.object.type.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.metavm.common.Result;
import org.metavm.object.type.TypeManager;
import org.metavm.object.type.rest.dto.TreeResponse;
import org.metavm.object.type.rest.dto.TypeTreeQuery;
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
        typeManager.deploy(appId, servletRequest.getInputStream());
        return Result.voidSuccess();
    }

    @GetMapping("/source-tag/**")
    public Result<Integer> getSourceTag(HttpServletRequest request) {
        var uri = request.getRequestURI();
        var name = uri.substring("/type/source-tag/".length()).replace('/', '.');
        return Result.success(typeManager.getSourceTag(name));
    }

}
