package tech.metavm.object.type.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.Flow;
import tech.metavm.flow.Method;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.type.*;
import tech.metavm.object.view.ObjectMapping;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ParameterizedTypeImpl;

import javax.annotation.Nullable;
import java.util.*;

import static tech.metavm.object.type.ResolutionStage.DEFINITION;
import static tech.metavm.object.type.ResolutionStage.INIT;

public class GenericContext implements ParameterizedFlowProvider, ParameterizedTypeRepository {

    private final Map<Klass, Map<List<? extends Type>, Klass>> parameterizedTypes = new HashMap<>();
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

    public Klass getParameterizedType(Klass template, Type... typeArguments) {
        return getParameterizedType(template, List.of(typeArguments));
    }

    public Klass getParameterizedType(Klass template, List<? extends Type> typeArguments) {
        return getParameterizedType(template, typeArguments, DEFINITION, emptyBatch);
    }

    public Klass getParameterizedType(Klass template, List<? extends Type> typeArguments, ResolutionStage stage) {
        return getParameterizedType(template, typeArguments, stage, emptyBatch);
    }

    public Klass getParameterizedType(Klass template, List<? extends Type> typeArguments, ResolutionStage stage, DTOProvider dtoProvider) {
        var existing = getExisting(template, typeArguments);
        if (existing == template)
            return template;
        if (existing != null && existing.getStage().isAfterOrAt(stage))
            return existing;
        var transformer = SubstitutorV2.create(
                template, template.getTypeParameters(), typeArguments, stage
        );
        return (Klass) template.accept(transformer);
    }

    @Override
    public List<Klass> getTemplateInstances(Klass template) {
        return entityContext.getTemplateInstances(template);
    }

    public Field retransformField(Field field, Klass existing) {
        var transformer = SubstitutorV2.create(field.getDeclaringType(),
                field.getDeclaringType().getTypeParameters(), existing.getTypeArguments(),
                DEFINITION);
        transformer.enterElement(existing);
        var transformedField = (Field) field.accept(transformer);
        transformer.exitElement();
        return transformedField;
    }

    public IEntityContext getEntityContext() {
        return entityContext;
    }

    public ObjectMapping retransformObjectMapping(ObjectMapping objectMapping, Klass parameterizedType) {
        var transformer = SubstitutorV2.create(objectMapping,
                objectMapping.getSourceKlass().getTypeParameters(), parameterizedType.getTypeArguments(),
                DEFINITION);
        transformer.enterElement(parameterizedType);
        var transformedMapping = (ObjectMapping) objectMapping.accept(transformer);
        transformer.exitElement();
        return transformedMapping;
    }

    public Method retransformMethod(Method methodTemplate, Klass parameterizedType) {
        var transformer = SubstitutorV2.create(methodTemplate.getDeclaringType(),
                methodTemplate.getDeclaringType().getTypeParameters(), parameterizedType.getTypeArguments(),
                DEFINITION);
        transformer.enterElement(parameterizedType);
        var transformedMethod = (Method) methodTemplate.accept(transformer);
        transformer.exitElement();
        return transformedMethod;
    }

    public Flow retransformHorizontalFlowInstances(Flow template, Flow templateInstance) {
        var transformer = SubstitutorV2.create(template,
                template.getTypeParameters(), templateInstance.getTypeArguments(),
                DEFINITION);
        if (templateInstance instanceof Method method)
            transformer.enterElement(method.getDeclaringType());
        return (Flow) template.accept(transformer);
    }

    public Klass retransformClass(Klass template, Klass parameterized) {
        parameterized.setStage(INIT);
        var transformer = SubstitutorV2.create(
                template, template.getTypeParameters(), parameterized.getTypeArguments(),
                DEFINITION
        );
        return (Klass) template.accept(transformer);
    }

    private Klass getNew(Klass template, List<? extends Type> typeArguments) {
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

    public Klass getExisting(Klass template, List<? extends Type> typeArguments) {
        if(parent != null && parent.getEntityContext().containsEntity(template)
                && NncUtils.allMatch(typeArguments, typeArg -> parent.getEntityContext().containsEntity(typeArg))) {
            var found = parent.getExisting(template, typeArguments);
            if(found != null)
                return found;
        }
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
        CompositeTypeEventRegistry.notifyFlowCreated(flow);
//        for (Type typeArgument : flow.getTypeArguments()) {
//            typeArgument.getCapturedTypes().forEach(ct -> ct.addCapturedFlow(flow));
//        }
    }

    public Klass load(Klass template, List<? extends Type> typeArguments) {
        if (entityContext == null) {
            return null;
        }
        return entityContext.selectFirstByKey(
                Klass.IDX_PARAMETERIZED_TYPE_KEY,
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

    public void add(Klass klass) {
        var template = klass.getEffectiveTemplate();
        if (template == null)
            return;
        CompositeTypeEventRegistry.notifyTypeCreated(klass.getType());
//        classType.getCapturedTypes().forEach(ct -> ct.addCapturedCompositeType(classType));
        parameterizedTypes.computeIfAbsent(template, k -> new HashMap<>()).put(klass.getTypeArguments(), klass);
        entityContext.tryBind(klass);
        if (typeFactory.isPutTypeSupported()) {
            var templateClass = (Class<?>) typeFactory.getJavaType(template.getType());
            if (templateClass != null) {
                var javaType = ParameterizedTypeImpl.create(
                        templateClass,
                        NncUtils.map(klass.getTypeArguments(), typeFactory::getJavaType)
                );
                typeFactory.putType(javaType, klass.getType());
            }
        }
        if (typeFactory.isAddTypeSupported()) {
            typeFactory.addType(klass.getType());
        }
    }

    public List<Klass> getNewTypes() {
        List<Klass> newTypes = new ArrayList<>();
        for (var value : parameterizedTypes.values()) {
            for (Klass classType : value.values()) {
                if (entityContext.isNewEntity(classType))
                    newTypes.add(classType);
            }
        }
        return newTypes;
    }

    public void generateDeclarations(Klass declaringType, Klass template) {
        if (declaringType.getStage().isAfterOrAt(ResolutionStage.DECLARATION)) {
            return;
        }
        if (template.getStage().isBefore(ResolutionStage.DECLARATION)) {
            throw new InternalException("Template declarations not generated yet");
        }
        var transformer = SubstitutorV2.create(
                template, template.getTypeParameters(), declaringType.getTypeArguments(),
                ResolutionStage.DECLARATION);
        template.accept(transformer);
    }

    public void generateDeclarations(Klass template) {
        var templateInstances = parameterizedTypes.get(template);
        if (templateInstances != null) {
            for (Klass templateInst : templateInstances.values()) {
                generateDeclarations(templateInst, template);
            }
        }
    }

    public void generateCode(Klass template) {
        var templateInstances = parameterizedTypes.get(template);
        if (templateInstances != null) {
            for (Klass templateInst : templateInstances.values()) {
                generateCode(templateInst, template);
            }
        }
    }

    public void generateCode(Klass declaringType, Klass template) {
        if (declaringType.getStage().isAfterOrAt(DEFINITION)) {
            return;
        }
        if (template.getStage().isBefore(ResolutionStage.DEFINITION)) {
            throw new InternalException("Template code not generated yet");
        }
        var substitutor = SubstitutorV2.create(template, template.getTypeParameters(),
                declaringType.getTypeArguments(), DEFINITION);
        template.accept(substitutor);
    }

    public <T extends Flow> T getParameterizedFlow(T template, List<? extends Type> typeArguments) {
        return getParameterizedFlow(template, typeArguments, DEFINITION, emptyBatch);
    }

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public <T extends Flow> T getParameterizedFlow(T template, List<? extends Type> typeArguments, ResolutionStage stage, SaveTypeBatch batch) {
        if(template.getTypeArguments().equals(typeArguments))
            return template;
        NncUtils.requireTrue(template.isTemplate(), () -> new InternalException(template.getQualifiedName() + " is not a flow template"));
        if (template.getTypeParameters().isEmpty())
            return template;
        var existing = getExistingFlow(template, typeArguments);
        if (existing != null && existing.getStage().isAfterOrAt(stage))
            return existing;
        var substitutor = SubstitutorV2.create(
                template, template.getTypeParameters(), typeArguments, stage
        );
        if (template instanceof Method method)
            substitutor.enterElement(method.getDeclaringType());
        if(DebugEnv.debugging && template.getName().equals("find") && NncUtils.anyMatch(typeArguments, Type::isCaptured))
            debugLogger.info("Start substituting flow: {} with type arguments: {}",
                    EntityUtils.getEntityPath(template),
                    NncUtils.join(typeArguments, EntityUtils::getEntityPath));
        var transformed = (Flow) template.accept(substitutor);
        if(DebugEnv.debugging && template.getName().equals("find") && NncUtils.anyMatch(typeArguments, Type::isCaptured))
            debugLogger.info("Finish substituting flow: {} with type arguments: {}",
                    EntityUtils.getEntityPath(template),
                    NncUtils.join(typeArguments, EntityUtils::getEntityPath));
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
                for (Klass pType : new ArrayList<>(values.values())) {
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
