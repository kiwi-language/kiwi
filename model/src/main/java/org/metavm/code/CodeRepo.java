package org.metavm.code;

import org.metavm.code.rest.dto.CodeRepoDTO;
import org.metavm.entity.Entity;
import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;

@EntityType
public class CodeRepo extends Entity {

    public static CodeRepo create(CodeRepoDTO codeRepoDTO, IEntityContext context) {
        var codeRepo = new CodeRepo(codeRepoDTO.url());
        context.bind(codeRepo);
        return codeRepo;
    }

    private String url;

    public CodeRepo(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
