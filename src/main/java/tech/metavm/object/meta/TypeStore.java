package tech.metavm.object.meta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityStore;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.mappers.InstanceMapper;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.persistence.mappers.FieldMapper;
import tech.metavm.object.meta.persistence.mappers.TypeMapper;
import tech.metavm.object.meta.persistence.query.TypeQuery;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class TypeStore implements EntityStore<Type> {

    public static final int MAX_NUM_OPTIONS = 64;

    @Autowired
    private TypeMapper typeMapper;

    @Autowired
    private FieldMapper fieldMapper;

    @Autowired
    private InstanceMapper instanceMapper;

    public Type getNullableType(Type baseType, EntityContext context) {
        TypePO typePO = typeMapper.selectByCategoryAndBaseId(TypeCategory.NULLABLE.code(), baseType.getId());
        return createFromPO(typePO, true, context);
    }

    public Type getByCategory(TypeCategory category, EntityContext context) {
        TypePO typePO = typeMapper.selectByCode(category.code());
        return createFromPO(typePO, true, context);
    }

    public Type getArrayType(Type baseType, EntityContext context) {
        TypePO arrayTypePO = typeMapper.selectByCategoryAndBaseId(TypeCategory.ARRAY.code(), baseType.getId());
        return createFromPO(arrayTypePO, true, context);
    }

    public Type getByName(String name, EntityContext context) {
        TypePO typePO = typeMapper.selectByName(context.getTenantId(), name);
        return createFromPO(typePO, true, context);
    }

    private Type createFromPO(TypePO typePO, boolean loadFieldsAndOptions, EntityContext context) {
        if(typePO == null) {
            return null;
        }
        return createFromPOs(List.of(typePO), loadFieldsAndOptions, context).get(0);
    }

    private List<Type> createFromPOs(List<TypePO> typePOs, boolean loadFieldsAndOptions, EntityContext context) {
        if(NncUtils.isEmpty(typePOs)) {
            return List.of();
        }
        List<Type> types = NncUtils.map(typePOs, typePO -> new Type(typePO, context));
        if(loadFieldsAndOptions) {
            loadFieldsAndOptions(types, context);
        }
        return types;
    }

    public long count(TypeQuery query) {
        return typeMapper.count(query);
    }

    public List<Type> query(TypeQuery query, EntityContext context) {
        List<TypePO> poList = typeMapper.query(query);
        if(NncUtils.isEmpty(poList)) {
            return List.of();
        }
        List<Type> types = NncUtils.map(
                poList,
                po -> new Type(po, context)
        );
        loadFieldsAndOptions(types, context);
        return types;
    }

    @Override
    public List<Type> batchGet(Collection<Long> ids, EntityContext context) {
        return batchGet(ids, true, context);
    }

    public List<Type> batchGet(Collection<Long> ids, boolean loadFields, EntityContext context) {
        if(NncUtils.isEmpty(ids)) {
            return List.of();
        }
        List<TypePO> typePOs = typeMapper.selectByIds(ids);
        return createFromPOs(typePOs, loadFields, context);
    }


    public void loadFieldsAndOptions(List<Type> types, EntityContext context) {
        long tenantId = context.getTenantId();
        List<FieldPO> fieldPOs = fieldMapper.selectByOwnerIds(tenantId, NncUtils.map(types, Type::getId));
        Map<Long, List<FieldPO>> fieldPOMap = NncUtils.toMultiMap(fieldPOs, FieldPO::getOwnerId);

        Set<Long> fieldTypeIds = NncUtils.mapUnique(fieldPOs, FieldPO::getTypeId);
        Map<Long, Type> fieldTypeMap = NncUtils.toMap(
                batchGet(fieldTypeIds, false, context),
                Entity::getId
        );

        List<Long> baseTypeIds = NncUtils.filterAndMap(
                fieldTypeMap.values(),
                type -> type.isArray() || type.isNullable(),
                type -> type.getBaseType().getId()
        );
        if(NncUtils.isNotEmpty(baseTypeIds)) {
            Set<Long> baseTypeIdSet = new HashSet<>(baseTypeIds);
            batchGet(baseTypeIdSet, false, context);
        }
        for (Type type : types) {
            List<ChoiceOption> options = new ArrayList<>();
            if(type.isEnum()) {
                List<InstancePO> instancePOs =
                        instanceMapper.selectByModelIds(tenantId, List.of(type.getId()), 0, MAX_NUM_OPTIONS);
                options.addAll(NncUtils.map(instancePOs, instancePO -> new ChoiceOption(instancePO, type)));
            }
            List<Field> fields = NncUtils.map(
                    fieldPOMap.get(type.getId()),
                    fieldPO -> new Field(fieldPO, type, fieldTypeMap.get(fieldPO.getTypeId()))
            );
            type.initFieldsAndOptions(fields, options);
        }
    }

    @Override
    public void batchInsert(List<Type> entities) {
        if(NncUtils.isNotEmpty(entities)) {
            typeMapper.batchInsert(NncUtils.map(entities, Type::toPO));
        }
    }

    @Override
    public int batchUpdate(List<Type> entities) {
        if(NncUtils.isNotEmpty(entities)) {
            return typeMapper.batchUpdate(NncUtils.map(entities, Type::toPO));
        }
        else {
            return 0;
        }
    }

    @Override
    public void batchDelete(List<Long> ids) {
        typeMapper.batchDelete(ids);
    }

    @Override
    public Class<Type> getEntityType() {
        return Type.class;
    }

}
