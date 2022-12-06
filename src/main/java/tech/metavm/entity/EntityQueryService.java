package tech.metavm.entity;

import org.springframework.stereotype.Component;
import tech.metavm.dto.Page;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

@Component
public class EntityQueryService {

    private final InstanceQueryService instanceQueryService;

    public EntityQueryService(InstanceQueryService instanceQueryService) {
        this.instanceQueryService = instanceQueryService;
    }

    public <T extends Entity> Page<T> query(EntityQuery<T> query, IEntityContext context) {
        InstanceQuery instanceQuery = convertToInstanceQuery(query);
        Page<Long> instancePage =  instanceQueryService.query(instanceQuery, context.getInstanceContext());
        return instancePage.map(id -> context.getEntity(query.entityType(), id));
    }

    private InstanceQuery convertToInstanceQuery(EntityQuery<?> entityQuery) {
        Type type = ModelDefRegistry.getType(entityQuery.entityType());
        return new InstanceQuery(
                type.getId(),
                entityQuery.searchText(),
                entityQuery.page(),
                entityQuery.pageSize(),
                NncUtils.map(entityQuery.fields(), f -> convertToInstanceQueryField(type ,f))
        );
    }

    private InstanceQueryField convertToInstanceQueryField(Type type, EntityQueryField entityQueryField) {
        EntityDef<?> entityDef = ModelDefRegistry.getEntityDef(type);
        Field field = entityDef.getFieldByJavaFieldName(entityQueryField.fieldName());
        return new InstanceQueryField(field, entityQueryField.value());
    }


}
