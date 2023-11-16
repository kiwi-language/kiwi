package tech.metavm.object.type.generic;

import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.*;
import tech.metavm.task.FieldData;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ParameterizedTypeImpl;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.object.type.ResolutionStage.DEFINITION;

public class GenericContext {

    private final Map<ClassType, Map<List<? extends Type>, ClassType>> map = new HashMap<>();
    private final IEntityContext entityContext;
    private final TypeFactory typeFactory;
    private final SaveTypeBatch emptyBatch;
    private final GenericContext parent;

    public GenericContext(IEntityContext entityContext, TypeFactory typeFactory, @Nullable  GenericContext parent) {
        this.entityContext = entityContext;
        this.typeFactory = typeFactory;
        this.parent = parent;
        emptyBatch = SaveTypeBatch.empty(entityContext);
    }

    public ClassType getParameterizedType(ClassType template, Type... typeArguments) {
        return getParameterizedType(template, List.of(typeArguments));
    }

    public ClassType getParameterizedType(ClassType template, List<? extends Type> typeArguments) {
        return getParameterizedType(template, typeArguments, DEFINITION, emptyBatch);
    }

    public ClassType getParameterizedType(ClassType template, List<? extends Type> typeArguments, ResolutionStage stage) {
        return getParameterizedType(template, typeArguments, stage, emptyBatch);
    }

    public ClassType getParameterizedType(ClassType template, List<? extends Type> typeArguments, ResolutionStage stage, SaveTypeBatch batch) {
        var existing = getExisting(template, typeArguments);
        if (existing == template)
            return template;
        if (existing != null && existing.getStage().isAfterOrAt(stage))
            return existing;
        var transformer = new SubstitutorV2(
                template, this, template.getTypeParameters(), typeArguments, stage, batch,
                typeFactory
        );
        return (ClassType) template.accept(transformer);
    }

    public Field retransformField(Field field, ClassType existing) {
        var transformer = new SubstitutorV2(field, this,
                field.getDeclaringType().getTypeParameters(), existing.getTypeArguments(),
                DEFINITION,
                SaveTypeBatch.empty(entityContext), typeFactory);
        transformer.enterElement(existing);
        var transformedField = (Field) field.accept(transformer);
        transformer.exitElement();
        return transformedField;
    }

    public IEntityContext getEntityContext() {
        return entityContext;
    }

    public Flow retransformFlow(Flow flowTemplate, ClassType parameterizedType) {
        var transformer = new SubstitutorV2(flowTemplate, this,
                flowTemplate.getDeclaringType().getTypeParameters(), parameterizedType.getTypeArguments(),
                DEFINITION,
                SaveTypeBatch.empty(entityContext), typeFactory);
        transformer.enterElement(parameterizedType);
        var transformedFlow = (Flow) flowTemplate.accept(transformer);
        transformer.exitElement();
        return transformedFlow;
    }

    public ClassType retransformClass(ClassType template, ClassType parameterized) {
        var transformer = new SubstitutorV2(
                template, this, template.getTypeParameters(), parameterized.getTypeArguments(),
                DEFINITION, emptyBatch, typeFactory
        );
        return (ClassType) template.accept(transformer);
    }

    public FieldData transformFieldData(ClassType template, ClassType parameterized, FieldData fieldData) {
        var transformer = new SubstitutorV2(
                fieldData, this, template.getTypeParameters(), parameterized.getTypeArguments(),
                DEFINITION, emptyBatch, typeFactory);
        transformer.enterElement(parameterized);
        var subst = (FieldData) transformer.copy(fieldData);
        transformer.exitElement();
        return subst;
    }

    private ClassType getNew(ClassType template, List<? extends Type> typeArguments) {
        if(parent != null) {
            var t = parent.getNew(template, typeArguments);
            if(t != null)
                return t;
        }
        return map.computeIfAbsent(template, k -> new HashMap<>()).get(typeArguments);
    }

    public ClassType getExisting(ClassType template, List<? extends Type> typeArguments) {
        var existing = getNew(template, typeArguments);
        if (existing != null)
            return existing;
        if (template.getId() != null && NncUtils.allMatch(typeArguments, typeArg -> typeArg.getId() != null)) {
            var loaded = load(template, typeArguments);
            if (loaded != null) {
                map.get(template).put(typeArguments, loaded);
                return loaded;
            }
        }
        return null;
    }

    public ClassType load(ClassType template, List<? extends Type> typeArguments) {
        if (entityContext == null) {
            return null;
        }
        return entityContext.selectByUniqueKey(
                ClassType.IDX_PARAMETERIZED_TYPE_KEY,
                ClassType.pTypeKey(template, typeArguments)
        );
    }

    public void addType(ClassType classType) {
        var template = classType.getEffectiveTemplate();
        if (template == null) {
            return;
        }
        map.computeIfAbsent(template, k -> new HashMap<>()).put(classType.getTypeArguments(), classType);
        entityContext.tryBind(classType);
        if (typeFactory.isPutTypeSupported()) {
            var templateClass = (Class<?>) typeFactory.getJavaType(template);
            if (templateClass != null) {
                var javaType = ParameterizedTypeImpl.create(
                        templateClass,
                        NncUtils.map(classType.getTypeArguments(), typeFactory::getJavaType)
                );
                typeFactory.putType(javaType, classType);
            }
        }
        if (typeFactory.isAddTypeSupported()) {
            typeFactory.addType(classType);
        }
    }

    public List<ClassType> getNewTypes() {
        List<ClassType> newTypes = new ArrayList<>();
        for (var value : map.values()) {
            for (ClassType classType : value.values()) {
                if (entityContext.isNewEntity(classType))
                    newTypes.add(classType);
            }
        }
        return newTypes;
    }

    public void generateDeclarations(ClassType declaringType, ClassType template) {
        if (declaringType.getStage().isAfterOrAt(ResolutionStage.DECLARATION)) {
            return;
        }
        if (template.getStage().isBefore(ResolutionStage.DECLARATION)) {
            throw new InternalException("Template declarations not generated yet");
        }
        var transformer = new SubstitutorV2(
                template, this, template.getTypeParameters(), declaringType.getTypeArguments(),
                ResolutionStage.DECLARATION, SaveTypeBatch.empty(entityContext), typeFactory);
        template.accept(transformer);
    }

    public void generateDeclarations(ClassType template) {
        var templateInstances = map.get(template);
        if (templateInstances != null) {
            for (ClassType templateInst : templateInstances.values()) {
                generateDeclarations(templateInst, template);
            }
        }
    }

    public void generateCode(ClassType template) {
        var templateInstances = map.get(template);
        if (templateInstances != null) {
            for (ClassType templateInst : templateInstances.values()) {
                generateCode(templateInst, template);
            }
        }
    }

    public void generateCode(ClassType declaringType, ClassType template) {
        if (declaringType.getStage().isAfterOrAt(DEFINITION)) {
            return;
        }
        if (template.getStage().isBefore(ResolutionStage.DEFINITION)) {
            throw new InternalException("Template code not generated yet");
        }
        var substitutor = new SubstitutorV2(template, this, template.getTypeParameters(),
                declaringType.getTypeArguments(), DEFINITION, SaveTypeBatch.empty(entityContext), typeFactory);
        template.accept(substitutor);
    }

    public Flow getParameterizedFlow(Flow template, List<Type> typeArguments) {
        return getParameterizedFlow(template, typeArguments, DEFINITION, emptyBatch);
    }

    public Flow getParameterizedFlow(Flow template, List<Type> typeArguments, ResolutionStage stage, SaveTypeBatch batch) {
        NncUtils.requireTrue(template.isTemplate(), "Not a flow template");
        if (template.getTypeParameters().isEmpty()) {
            return template;
        }
        var ti = template.getTemplateInstance(typeArguments);
        if (ti != null) {
            return ti;
        }
        var substitutor = new SubstitutorV2(
                template, this, template.getTypeParameters(), typeArguments, stage,
                batch, typeFactory
        );
        substitutor.enterElement(template.getDeclaringType());
        var transformed = (Flow) template.accept(substitutor);
        substitutor.exitElement();
        return transformed;
    }


}
