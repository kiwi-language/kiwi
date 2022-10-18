package tech.metavm.object.meta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.*;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.mappers.InstanceMapper;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.persistence.mappers.FieldMapper;
import tech.metavm.object.meta.persistence.mappers.TypeMapper;
import tech.metavm.object.meta.persistence.query.TypeQuery;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class TypeStore implements EntityStore<Type> {

    public static final long MAX_NUM_OPTIONS = 64;

    @Autowired
    private TypeMapper typeMapper;

    @Autowired
    private FieldMapper fieldMapper;

    @Autowired
    private InstanceMapper instanceMapper;

    public Type getNullableType(Type baseType, EntityContext context) {
        if(baseType.getId() == null) {
            return null;
        }
        TypePO typePO = typeMapper.selectByCategoryAndBaseId(TypeCategory.NULLABLE.code(), baseType.getId());
        return createFromPO(typePO, context, LoadingOption.none());
    }

    public Type getByCategory(TypeCategory category, EntityContext context) {
        TypePO typePO = typeMapper.selectByCode(category.code());
        return createFromPO(typePO, context, LoadingOption.none());
    }

    public List<Type> getByBaseType(Type baseType, EntityContext context) {
        List<TypePO> typePOs = typeMapper.selectByBaseTypeId(baseType.getTenantId(), baseType.getId());
        return createFromPOs(typePOs, context, LoadingOption.none());
    }

    public Type getArrayType(Type baseType, EntityContext context) {
        if(baseType.getId() == null) {
            return null;
        }
        TypePO arrayTypePO = typeMapper.selectByCategoryAndBaseId(TypeCategory.ARRAY.code(), baseType.getId());
        return createFromPO(arrayTypePO, context, LoadingOption.none());
    }

    public Type getByName(String name, EntityContext context) {
        TypePO typePO = typeMapper.selectByName(context.getTenantId(), name);
        return createFromPO(typePO, context, LoadingOption.none());
    }

    private Type createFromPO(TypePO typePO, EntityContext context, EnumSet<LoadingOption> options) {
        if(typePO == null) {
            return null;
        }
        return createFromPOs(List.of(typePO), context, options).get(0);
    }

    private List<Type> createFromPOs(List<TypePO> typePOs, EntityContext context, EnumSet<LoadingOption> options) {
        if(NncUtils.isEmpty(typePOs)) {
            return List.of();
        }
        Set<Long> baseTypeIds = NncUtils.mapAndFilterUnique(typePOs, TypePO::getBaseTypeId, Objects::nonNull);
        context.batchGet(Type.class, baseTypeIds, options);

        List<Long> enumIds = NncUtils.filterAndMap(
                typePOs,
                typePO -> typePO.getCategory() == TypeCategory.ENUM.code(),
                TypePO::getId
        );

        Map<Long, List<InstancePO>> optionMap =
                NncUtils.toMultiMap(loadChoiceOptionPOs(enumIds, context), InstancePO::modelId);

        List<Type> types = NncUtils.map(typePOs, typePO -> new Type(typePO, optionMap.get(typePO.getId()), context));
        List<Type> pojoOrEnums = NncUtils.filter(types, t -> hasFieldsOrOptions(t.getCategory()));
        if(!options.contains(LoadingOption.FIELDS_LAZY_LOADING) && NncUtils.isNotEmpty(pojoOrEnums)) {
            loadFields(pojoOrEnums, context);
        }
        return types;
    }

    private boolean hasFieldsOrOptions(TypeCategory category) {
        return category == TypeCategory.ENUM || category.hasFields();
    }

    public List<Field> getFieldsLoadingList(Type type) {
        return new LoadingList<>(() -> loadFields(List.of(type)));
    }

    public List<ChoiceOption> getChoiceOptionsLoadingList(Type type) {
        return new LoadingList<>(() -> loadChoiceOptions(List.of(type)));
    }

    private List<Field> loadFields(List<Type> types) {
        if(NncUtils.isEmpty(types)) {
            return List.of();
        }
        EntityContext context = types.get(0).getContext();
        Map<Long, Type> typeMap = NncUtils.toMap(types, Entity::getId);
        List<FieldPO> fieldPOs = fieldMapper.selectByOwnerIds(context.getTenantId(), NncUtils.map(types, Entity::getId));
        return NncUtils.map(
                fieldPOs,
                fieldPO -> new Field(fieldPO, typeMap.get(fieldPO.getOwnerId()), context.getType(fieldPO.getTypeId()))
        );
    }


    private List<InstancePO> loadChoiceOptionPOs(List<Long> typeIds, EntityContext context) {
        if(NncUtils.isEmpty(typeIds)) {
            return List.of();
        }
        return instanceMapper.selectByModelIds(
                context.getTenantId(),
                typeIds,
                0, MAX_NUM_OPTIONS * typeIds.size()
        );
    }

    private List<ChoiceOption> loadChoiceOptions(List<Type> types) {
        types = NncUtils.filter(types, Type::isEnum);
        if(NncUtils.isEmpty(types)) {
            return List.of();
        }
        EntityContext context = types.get(0).getContext();
        Map<Long, Type> typeMap = NncUtils.toMap(types, Entity::getId);
        List<InstancePO> instancePOs = instanceMapper.selectByModelIds(
                context.getTenantId(),
                NncUtils.map(types, Entity::getId),
                0, MAX_NUM_OPTIONS * types.size()
        );
        return NncUtils.map(instancePOs, instancePO -> new ChoiceOption(instancePO, typeMap.get(instancePO.modelId())));
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
                po -> new Type(po, List.of(), context)
        );
        loadFields(types, context);
        return types;
    }

    @Override
    public List<Type> batchGet(Collection<Long> ids, EntityContext context, EnumSet<LoadingOption> options) {
        if(NncUtils.isEmpty(ids)) {
            return List.of();
        }
        List<TypePO> typePOs = typeMapper.selectByIds(ids);
        return createFromPOs(typePOs, context, options);
    }

    public void loadFields(List<Type> types, EntityContext context) {
        List<Field> fields = loadFields(types);
        Map<Long, List<Field>> fieldMap = NncUtils.toMultiMap(fields, f -> f.getOwner().getId());
        Set<Long> fieldTypeIds = NncUtils.mapUnique(fields, f -> f.getType().getId());
        context.batchGet(Type.class, fieldTypeIds, LoadingOption.FIELDS_LAZY_LOADING);

        for (Type type : types) {
            type.preloadFields(fieldMap.computeIfAbsent(type.getId(), k -> new ArrayList<>()));
        }
    }

    @Override
    public void batchInsert(List<Type> entities) {
        if(NncUtils.isNotEmpty(entities)) {
            typeMapper.batchInsert(NncUtils.map(entities, Type::toPO));
            List<ChoiceOption> choiceOptions = NncUtils.flatMap(entities, Type::getChoiceOptions);
            if(NncUtils.isNotEmpty(choiceOptions)) {
                instanceMapper.batchInsert(NncUtils.map(choiceOptions, ChoiceOption::toPO));
            }
        }
    }

    @Override
    public int batchUpdate(List<Type> entities) {
        if(NncUtils.isNotEmpty(entities)) {
            long tenantId = entities.get(0).getTenantId();
            int affected = typeMapper.batchUpdate(NncUtils.map(entities, Type::toPO));
            List<Type> enumTypes = NncUtils.filter(entities, Type::isEnum);
            if(NncUtils.isNotEmpty(enumTypes)) {
                List<Long> enumIds = NncUtils.map(enumTypes, Entity::getId);
                List<ChoiceOption> options = NncUtils.flatMap(enumTypes, Type::getChoiceOptions);
                List<InstancePO> optionPOs = NncUtils.map(options, ChoiceOption::toPO);
                List<InstancePO> oldOptionPOs =
                        instanceMapper.selectByModelIds(tenantId, enumIds, 0, MAX_NUM_OPTIONS * enumTypes.size());
                ChangeList<InstancePO> optionChange = ChangeList.build(oldOptionPOs, optionPOs, InstancePO::id);
                if(NncUtils.isNotEmpty(optionChange.inserts())) {
                    instanceMapper.batchInsert(optionChange.inserts());
                }
                if(NncUtils.isNotEmpty(optionChange.updates())) {
                    instanceMapper.batchUpdate(optionChange.updates());
                }
                if(NncUtils.isNotEmpty(optionChange.deletes())) {
                    instanceMapper.batchDelete(
                            tenantId,
                            NncUtils.map(optionChange.deletes(), InstancePO::nextVersion)
                    );
                }
            }
            return affected;
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
