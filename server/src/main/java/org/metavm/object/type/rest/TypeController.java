package org.metavm.object.type.rest;

import org.metavm.context.http.*;
import org.metavm.object.type.TypeManager;
import org.metavm.object.type.rest.dto.GetSourceTagRequest;

import java.io.IOException;
import java.io.InputStream;

@Controller
@Mapping("/type")
public class TypeController {

    private final TypeManager typeManager;

    public TypeController(TypeManager typeManager) {
        this.typeManager = typeManager;
    }

    @Post("/deploy/{appId}")
    public String deploy(InputStream input, @PathVariable("appId") long appId,
                         @RequestParam(value = "no-backup", defaultValue = "false") boolean noBackup) throws IOException {
        typeManager.ensureAppAccess(appId);
        return typeManager.deploy(appId, noBackup, input);
    }

    @Post("/source-tag")
    public int etSourceTag(@RequestBody GetSourceTagRequest request) {
        return typeManager.getSourceTag(request.appId(), request.name());
    }

}
