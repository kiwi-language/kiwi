package tech.metavm.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityStore;
import tech.metavm.entity.LoadingOption;
import tech.metavm.flow.persistence.ScopeMapper;
import tech.metavm.flow.persistence.ScopePO;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class ScopeStore implements EntityStore<ScopeRT> {

    @Autowired
    private ScopeMapper scopeMapper;

    @Override
    public List<ScopeRT> batchGet(Collection<Long> ids, EntityContext context, Set<LoadingOption> options) {
        List<ScopePO> scopePOs = scopeMapper.batchSelect(ids);
        Set<Long> flowIds = NncUtils.mapUnique(scopePOs, ScopePO::getFlowId);
        List<FlowRT> flows = context.batchGet(FlowRT.class, flowIds, options);
        Map<Long, FlowRT> flowMap = NncUtils.toEntityMap(flows);
        List<ScopeRT> scopes = new ArrayList<>();
        for (ScopePO scopePO : scopePOs) {
            ScopeRT scope = NncUtils.get(flowMap.get(scopePO.getFlowId()), flow -> flow.getScope(scopePO.getId()));
            if(scope != null) {
                scopes.add(scope);
            }
        }
        return scopes;
    }

    @Override
    public void batchInsert(List<ScopeRT> entities) {
        scopeMapper.batchInsert(NncUtils.map(entities, ScopeRT::toPO));
    }

    @Override
    public int batchUpdate(List<ScopeRT> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void batchDelete(List<ScopeRT> scopes) {
        scopeMapper.batchDelete(NncUtils.map(scopes, Entity::getId));
    }

    @Override
    public Class<ScopeRT> getEntityType() {
        return ScopeRT.class;
    }
}
