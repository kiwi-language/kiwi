package tech.metavm.entity;

import org.springframework.stereotype.Component;
import tech.metavm.common.Page;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.util.NncUtils;

import java.util.Collection;

@Component
public class EntityQueryService {

    private final InstanceQueryService instanceQueryService;

    public EntityQueryService(InstanceQueryService instanceQueryService) {
        this.instanceQueryService = instanceQueryService;
    }

    public <T extends Entity> Page<T> query(EntityQuery<T> query, IEntityContext context) {
        InstanceQuery instanceQuery = convertToInstanceQuery(query, context);
        Page<Instance> instancePage = instanceQueryService.query(instanceQuery, context.getInstanceContext());
        return instancePage.map(inst -> context.getEntity(query.entityType(), inst));
    }

    public <T extends Entity> long count(EntityQuery<T> query, IEntityContext context) {
        InstanceQuery instanceQuery = convertToInstanceQuery(query, context);
        return instanceQueryService.count(instanceQuery, context.getInstanceContext());
    }

    private InstanceQuery convertToInstanceQuery(EntityQuery<?> entityQuery, IEntityContext context) {
        ClassType type = ModelDefRegistry.getClassType(entityQuery.entityType());
        EntityDef<?> entityDef = ModelDefRegistry.getEntityDef(type);
        return new InstanceQuery(
                type,
                entityQuery.searchText(),
                entityQuery.expression(),
                NncUtils.map(entityQuery.searchFields(), entityDef::getFieldByJavaFieldName),
                entityQuery.includeBuiltin(),
                true,
                entityQuery.page(),
                entityQuery.pageSize(),
                NncUtils.map(entityQuery.fields(), f -> convertToInstanceQueryField(entityDef, f, context)),
                entityQuery.newlyCreated(),
                entityQuery.excluded()
        );
    }

    private InstanceQueryField convertToInstanceQueryField(EntityDef<?> entityDef, EntityQueryField entityQueryField, IEntityContext context) {
        Field field = entityDef.getFieldByJavaFieldName(entityQueryField.fieldName());
        Instance instanceValue = convertValue(entityQueryField.value(), context);
        return new InstanceQueryField(field, instanceValue, null, null);
    }

    private Instance convertValue(Object value, IEntityContext context) {
        if (context.containsModel(value)) {
            return context.getInstance(value);
        } else if (value instanceof Collection<?> coll) {
            return new ArrayInstance(
                    StandardTypes.getObjectArrayType(),
                    NncUtils.map(
                            coll,
                            item -> convertSingleValue(item, context)
                    )
            );
        } else {
            return convertSingleValue(value, context);
        }
    }

    private Instance convertSingleValue(Object value, IEntityContext context) {
        return context.getInstance(value);
    }

}
