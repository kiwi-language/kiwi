package tech.metavm.object.type.generic;

import tech.metavm.entity.Entity;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.Flow;
import tech.metavm.flow.Method;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.type.*;
import tech.metavm.object.view.ObjectMapping;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ParameterizedTypeImpl;

import javax.annotation.Nullable;
import java.util.*;

import static tech.metavm.object.type.ResolutionStage.DEFINITION;
import static tech.metavm.object.type.ResolutionStage.INIT;

public class GenericContext implements ParameterizedFlowProvider, ParameterizedTypeProvider {

    private final Map<ClassType, Map<List<? extends Type>, ClassType>> parameterizedTypes = new HashMap<>();
    private final Map<Flow, Map<List<? extends Type>, Flow>> parameterizedFlows = new HashMap<>();
    private final Set<Flow> newFlows = new HashSet<>();
    private final IEntityContext entityContext;
    private final TypeFactory typeFactory;
    private final SaveTypeBatch emptyBatch;
    private final GenericContext parent;

    public GenericContext(IEntityContext entityContext, TypeFactory typeFactory, @Nullable GenericContext parent) {
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

    public ClassType getParameterizedType(ClassType template, List<? extends Type> typeArguments, ResolutionStage stage, DTOProvider dtoProvider) {
        var existing = getExisting(template, typeArguments);
        if (existing == template)
            return template;
        if (existing != null && existing.getStage().isAfterOrAt(stage))
            return existing;
        var transformer = SubstitutorV2.create(
                template, template.getTypeParameters(), typeArguments, stage, entityContext, dtoProvider
        );
        return (ClassType) template.accept(transformer);
    }

    @Override
    public List<ClassType> getTemplateInstances(ClassType template) {
        return entityContext.getTemplateInstances(template);
    }

    public Field retransformField(Field field, ClassType existing) {
        var transformer = SubstitutorV2.create(field.getDeclaringType(),
                field.getDeclaringType().getTypeParameters(), existing.getTypeArguments(),
                DEFINITION,
                entityContext,
                SaveTypeBatch.empty(entityContext));
        transformer.enterElement(existing);
        var transformedField = (Field) field.accept(transformer);
        transformer.exitElement();
        return transformedField;
    }

    public IEntityContext getEntityContext() {
        return entityContext;
    }

    public ObjectMapping retransformObjectMapping(ObjectMapping objectMapping, ClassType parameterizedType) {
        var transformer = SubstitutorV2.create(objectMapping,
                objectMapping.getSourceType().getTypeParameters(), parameterizedType.getTypeArguments(),
                DEFINITION,
                entityContext,
                SaveTypeBatch.empty(entityContext));
        transformer.enterElement(parameterizedType);
        var transformedMapping = (ObjectMapping) objectMapping.accept(transformer);
        transformer.exitElement();
        return transformedMapping;
    }

    public Method retransformMethod(Method methodTemplate, ClassType parameterizedType) {
        var transformer = SubstitutorV2.create(methodTemplate.getDeclaringType(),
                methodTemplate.getDeclaringType().getTypeParameters(), parameterizedType.getTypeArguments(),
                DEFINITION,
                entityContext,
                SaveTypeBatch.empty(entityContext));
        transformer.enterElement(parameterizedType);
        var transformedMethod = (Method) methodTemplate.accept(transformer);
        transformer.exitElement();
        return transformedMethod;
    }

    public Flow retransformHorizontalFlowInstances(Flow template, Flow templateInstance) {
        var transformer = SubstitutorV2.create(template,
                template.getTypeParameters(), templateInstance.getTypeArguments(),
                DEFINITION,
                entityContext,
                SaveTypeBatch.empty(entityContext));
        if (templateInstance instanceof Method method)
            transformer.enterElement(method.getDeclaringType());
        return (Flow) template.accept(transformer);
    }

    public ClassType retransformClass(ClassType template, ClassType parameterized) {
        parameterized.setStage(INIT);
        var transformer = SubstitutorV2.create(
                template, template.getTypeParameters(), parameterized.getTypeArguments(),
                DEFINITION, entityContext, emptyBatch
        );
        return (ClassType) template.accept(transformer);
    }

    private ClassType getNew(ClassType template, List<? extends Type> typeArguments) {
        if (parent != null) {
            var t = parent.getNew(template, typeArguments);
            if (t != null)
                return t;
        }
        return parameterizedTypes.computeIfAbsent(template, k -> new HashMap<>()).get(typeArguments);
    }

    private Flow getNewFlow(Flow template, List<? extends Type> typeArguments) {
        if (parent != null) {
            var t = parent.getNewFlow(template, typeArguments);
            if (t != null)
                return t;
        }
        return parameterizedFlows.computeIfAbsent(template, k -> new HashMap<>()).get(typeArguments);
    }

    public ClassType getExisting(ClassType template, List<? extends Type> typeArguments) {
        var existing = getNew(template, typeArguments);
        if (existing != null)
            return existing;
        if (template.tryGetId() != null && NncUtils.allMatch(typeArguments, typeArg -> typeArg.tryGetId() != null)) {
            var loaded = load(template, typeArguments);
            if (loaded != null) {
                parameterizedTypes.get(template).put(typeArguments, loaded);
                return loaded;
            }
        }
        return null;
    }

    public <T extends Flow> T getExistingFlow(T template, List<? extends Type> typeArguments) {
        var existing = getNewFlow(template, typeArguments);
        if (existing != null)
            //noinspection unchecked
            return (T) existing;
        if (template.tryGetId() != null && NncUtils.allMatch(typeArguments, typeArg -> typeArg.tryGetId() != null)) {
            var loaded = loadFlow(template, typeArguments);
            if (loaded != null) {
                parameterizedFlows.get(template).put(typeArguments, loaded);
                return (T) loaded;
            }
        }
        return null;
    }

    @Override
    public void add(Flow flow) {
        entityContext.tryBind(flow);
        parameterizedFlows.computeIfAbsent(flow.getEffectiveHorizontalTemplate(),
                k -> new HashMap<>()).put(flow.getTypeArguments(), flow);
    }

    public ClassType load(ClassType template, List<? extends Type> typeArguments) {
        if (entityContext == null) {
            return null;
        }
        return entityContext.selectFirstByKey(
                ClassType.IDX_PARAMETERIZED_TYPE_KEY,
                Types.getParameterizedKey(template, typeArguments)
        );
    }

    public Flow loadFlow(Flow template, List<? extends Type> typeArguments) {
        if (entityContext == null) {
            return null;
        }
        return entityContext.selectFirstByKey(
                Flow.IDX_PARAMETERIZED_KEY,
                Types.getParameterizedKey(template, typeArguments)
        );
    }

    public void add(ClassType classType) {
        var template = classType.getEffectiveTemplate();
        if (template == null) {
            return;
        }
        parameterizedTypes.computeIfAbsent(template, k -> new HashMap<>()).put(classType.getTypeArguments(), classType);
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
        for (var value : parameterizedTypes.values()) {
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
        var transformer = SubstitutorV2.create(
                template, template.getTypeParameters(), declaringType.getTypeArguments(),
                ResolutionStage.DECLARATION, entityContext, emptyBatch);
        template.accept(transformer);
    }

    public void generateDeclarations(ClassType template) {
        var templateInstances = parameterizedTypes.get(template);
        if (templateInstances != null) {
            for (ClassType templateInst : templateInstances.values()) {
                generateDeclarations(templateInst, template);
            }
        }
    }

    public void generateCode(ClassType template) {
        var templateInstances = parameterizedTypes.get(template);
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
        var substitutor = SubstitutorV2.create(template, template.getTypeParameters(),
                declaringType.getTypeArguments(), DEFINITION, entityContext, emptyBatch);
        template.accept(substitutor);
    }

    public <T extends Flow> T getParameterizedFlow(T template, List<? extends Type> typeArguments) {
        return getParameterizedFlow(template, typeArguments, DEFINITION, emptyBatch);
    }

    public <T extends Flow> T getParameterizedFlow(T template, List<? extends Type> typeArguments, ResolutionStage stage, SaveTypeBatch batch) {
        if(template.getTypeArguments().equals(typeArguments))
            return template;
        NncUtils.requireTrue(template.isTemplate(), "Not a flow template");
        if (template.getTypeParameters().isEmpty())
            return template;
        var existing = getExistingFlow(template, typeArguments);
        if (existing != null && existing.getStage().isAfterOrAt(stage))
            return existing;
        var substitutor = SubstitutorV2.create(
                template, template.getTypeParameters(), typeArguments, stage,
                entityContext, batch
        );
        if (template instanceof Method method)
            substitutor.enterElement(method.getDeclaringType());
        var transformed = (Flow) template.accept(substitutor);
        if (template instanceof Method)
            substitutor.exitElement();
        if(existing == null)
            newFlows.add(transformed);
        //noinspection unchecked
        return (T) transformed;
    }

    public void ensureAllDefined() {
        boolean allDefined = false;
        while (!allDefined) {
            allDefined = true;
            for (var values : new ArrayList<>(parameterizedTypes.values())) {
                for (ClassType pType : new ArrayList<>(values.values())) {
                    if (pType.getTemplate() != null && pType.getStage() != ResolutionStage.DEFINITION) {
                        getParameterizedType(pType.getTemplate(), pType.getTypeArguments(), ResolutionStage.DEFINITION, emptyBatch);
                        allDefined = false;
                    }
                }
            }
        }
    }

    public Set<Flow> getNewFlows() {
        return newFlows;
    }

}
