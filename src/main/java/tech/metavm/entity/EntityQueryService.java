package tech.metavm.entity;

import org.springframework.stereotype.Component;
import tech.metavm.dto.Page;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

import java.util.Collection;

@Component
public class EntityQueryService {

    private final InstanceQueryService instanceQueryService;

    public EntityQueryService(InstanceQueryService instanceQueryService) {
        this.instanceQueryService = instanceQueryService;
    }

    public <T extends Entity> Page<T> query(EntityQuery<T> query, IEntityContext context) {
        InstanceQuery instanceQuery = convertToInstanceQuery(query, context);
        Page<Long> instancePage =  instanceQueryService.query(instanceQuery, context.getInstanceContext());
        return instancePage.map(id -> context.getEntity(query.entityType(), id));
    }

    private InstanceQuery convertToInstanceQuery(EntityQuery<?> entityQuery, IEntityContext context) {
        ClassType type = ModelDefRegistry.getClassType(entityQuery.entityType());
        EntityDef<?> entityDef = ModelDefRegistry.getEntityDef(type);
        return new InstanceQuery(
                type.getIdRequired(),
                entityQuery.searchText(),
                NncUtils.map(entityQuery.searchFields(), entityDef::getFieldByJavaFieldName),
                entityQuery.includeBuiltin(),
                entityQuery.page(),
                entityQuery.pageSize(),
                NncUtils.map(entityQuery.fields(), f -> convertToInstanceQueryField(f, context))
        );
    }

    private InstanceQueryField convertToInstanceQueryField(EntityQueryField entityQueryField, IEntityContext context) {
        ClassType declaringType = ModelDefRegistry.getClassType(entityQueryField.field().getDeclaringClass());
        var entityDef = ModelDefRegistry.getEntityDef(declaringType);
        Field field = entityDef.getFieldByJavaFieldName(entityQueryField.field().getName());
        Object instanceValue = convertValue(entityQueryField.value(), context);
        return new InstanceQueryField(field, instanceValue);
    }

    private Object convertValue(Object value, IEntityContext context) {
        if(context.containsModel(value)) {
            return context.getInstance(value);
        }
        else if(value instanceof Collection<?> collection) {
            return NncUtils.map(
                    collection,
                    item -> convertSingleValue(item, context)
            );
        }
        else {
            return convertSingleValue(value, context);
        }
    }

    private Object convertSingleValue(Object value, IEntityContext context) {
        if(ValueUtil.isPrimitive(value)) {
            return InstanceUtils.primitiveInstance(value);
        }
        if(context.containsModel(value)) {
            return context.getInstance(value);
        }
        throw new InternalException("Can not convert query field value '" + value + "'");
    }

}
