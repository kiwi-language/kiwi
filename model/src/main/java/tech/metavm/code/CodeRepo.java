package tech.metavm.code;

import tech.metavm.code.rest.dto.CodeRepoDTO;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;

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
