package org.metavm.entity;

import org.metavm.common.Page;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ContextTypeDefRepository;
import org.metavm.object.type.Field;
import org.metavm.object.type.Types;
import org.metavm.util.NncUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class EntityQueryService {

    private final InstanceQueryService instanceQueryService;

    public EntityQueryService(InstanceQueryService instanceQueryService) {
        this.instanceQueryService = instanceQueryService;
    }

    public <T extends Entity> Page<T> query(EntityQuery<T> query, IEntityContext context) {
        InstanceQuery instanceQuery = convertToInstanceQuery(query, context);
        var idPage = instanceQueryService.query(instanceQuery,
                context.getInstanceContext(),
                new ContextTypeDefRepository(context)
        );
        return new Page<>(
                NncUtils.map(idPage.data(), inst -> context.getEntity(query.entityType(), inst.getId())),
                idPage.total()
        );
    }

    public <T extends Entity> long count(EntityQuery<T> query, IEntityContext context) {
        InstanceQuery instanceQuery = convertToInstanceQuery(query, context);
        return instanceQueryService.count(instanceQuery, context);
    }

    private InstanceQuery convertToInstanceQuery(EntityQuery<?> entityQuery, IEntityContext context) {
        EntityDef<?> entityDef = (EntityDef<?>) ModelDefRegistry.getDef(entityQuery.entityType());
        return new InstanceQuery(
                entityDef.getKlass(),
                entityQuery.searchText(),
                entityQuery.expression(),
                NncUtils.map(entityQuery.searchFields(), entityDef::getFieldByJavaFieldName),
                entityQuery.includeBuiltin(),
                true,
                entityQuery.page(),
                entityQuery.pageSize(),
                NncUtils.map(entityQuery.fields(), f -> convertToInstanceQueryField(entityDef, f, context)),
                NncUtils.map(entityQuery.newlyCreated(), Id::parse),
                NncUtils.map(entityQuery.excluded(), Id::parse),
                null
        );
    }

    private InstanceQueryField convertToInstanceQueryField(EntityDef<?> entityDef, EntityQueryField entityQueryField, IEntityContext context) {
        Field field = entityDef.getFieldByJavaFieldName(entityQueryField.fieldName());
        Value instanceValue = convertValue(entityQueryField.value(), context);
        return new InstanceQueryField(field, instanceValue, null, null);
    }

    private Value convertValue(Object value, IEntityContext context) {
        if (context.containsEntity(value)) {
            return context.getInstance(value).getReference();
        } else if (value instanceof Collection<?> coll) {
            return new ArrayInstance(
                    Types.getAnyArrayType(),
                    NncUtils.map(
                            coll,
                            item -> convertSingleValue(item, context)
                    )
            ).getReference();
        } else {
            return convertSingleValue(value, context);
        }
    }

    private Value convertSingleValue(Object value, IEntityContext context) {
        return context.getObjectInstanceMap().getInstance(value);
    }

}
