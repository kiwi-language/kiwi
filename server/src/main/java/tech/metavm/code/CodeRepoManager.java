package tech.metavm.code;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.code.rest.dto.CodeRepoDTO;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.EntityContextFactoryBean;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;

@Component
public class CodeRepoManager extends EntityContextFactoryBean {

    public CodeRepoManager(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
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

}
