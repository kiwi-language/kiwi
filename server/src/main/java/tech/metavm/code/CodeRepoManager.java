package tech.metavm.code;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.code.rest.dto.CodeRepoDTO;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;

@Component
public class CodeRepoManager {

    private final InstanceContextFactory contextFactory;

    public CodeRepoManager(InstanceContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    @Transactional
    public long create(CodeRepoDTO codeRepoDTO) {
        try(var context = newContext()) {
            var repo = CodeRepo.create(codeRepoDTO, context);
            context.finish();
            return repo.getIdRequired();
        }
    }

    @Transactional
    public void remove(long id) {
        try(var context = newContext()) {
            var repo = context.getEntity(CodeRepo.class, id);
            context.remove(repo);
            context.finish();
        }
    }

    public IEntityContext newContext() {
        return contextFactory.newEntityContext();
    }
}
