package org.metavm.code;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.metavm.code.rest.dto.CodeRepoDTO;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;

@Component
public class CodeRepoManager extends EntityContextFactoryAware {

    public CodeRepoManager(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    @Transactional
    public String create(CodeRepoDTO codeRepoDTO) {
        try(var context = newContext()) {
            var repo = CodeRepo.create(codeRepoDTO, context);
            context.finish();
            return repo.getStringId();
        }
    }

    @Transactional
    public void remove(String id) {
        try(var context = newContext()) {
            var repo = context.getEntity(CodeRepo.class, id);
            context.remove(repo);
            context.finish();
        }
    }

}
