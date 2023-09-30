package tech.metavm.object.meta.generic;

import tech.metavm.entity.GenericDeclaration;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.flow.Flow;
import tech.metavm.object.meta.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ParameterizedTypeImpl;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static tech.metavm.util.NncUtils.requireNonNull;
import static tech.metavm.util.NncUtils.zip;

public class GenericContext {

    private final Map<ClassType, Map<List<Type>, ClassType>> map = new HashMap<>();
    private final @Nullable IEntityContext entityContext;
    private final TypeFactory typeFactory;
    private final List<Consumer<ClassType>> callbacks = new ArrayList<>();

    public GenericContext(@Nullable IEntityContext entityContext) {
        this(entityContext, new DefaultTypeFactory(ModelDefRegistry::getType));
    }

    public GenericContext(@Nullable IEntityContext entityContext, TypeFactory typeFactory) {
        this.entityContext = entityContext;
        this.typeFactory = typeFactory;
    }

    public ClassType getParameterizedType(ClassType template, Type... typeArguments) {
        return getParameterizedType(template, List.of(typeArguments));
    }

    public ClassType getParameterizedType(ClassType template, List<Type> typeArguments) {
        var transformed = (ClassType) createTransformer(template, template.stage, typeArguments, typeFactory).transformType(template);
        transformed.stage = template.stage;
        return transformed;
    }

    public ClassType getExisting(ClassType template, List<Type> typeArguments) {
        var existing = map.computeIfAbsent(template, k -> new HashMap<>()).get(typeArguments);
        if (existing != null) {
            return existing;
        }
        if (template.getId() != null && NncUtils.allMatch(typeArguments, typeArg -> typeArg.getId() != null)) {
            var loaded = load(template, typeArguments);
            if (loaded != null) {
                map.get(template).put(typeArguments, loaded);
                return loaded;
            }
        }
        return null;
    }

    public ClassType load(ClassType template, List<Type> typeArguments) {
        if (entityContext == null) {
            return null;
        }
        return entityContext.selectByUniqueKey(
                ClassType.IDX_PARAMETERIZED_TYPE_KEY,
                ClassType.pTypeKey(template, typeArguments)
        );
    }

    private GenericTransformer createTransformer(GenericDeclaration genericDeclarator,
                                                 ResolutionStage stage,
                                                 List<Type> typeArguments,
                                                 TypeFactory typeFactory) {
        return createTransformer(new MetaSubstitutor(zip(genericDeclarator.getTypeParameters(), typeArguments)),
                stage,
                typeFactory);
    }

    private GenericTransformer createTransformer(MetaSubstitutor substitutor, ResolutionStage stage, TypeFactory typeFactory) {
        return new GenericTransformer(
                substitutor,
                stage,
                typeFactory,
                this,
                this::postProcess
        );
    }

    private void postProcess(ClassType classType) {
        var template = classType.getTemplate();
        if (template == null) {
            return;
        }
        map.get(template).put(classType.getTypeArguments(), classType);
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
        for (Consumer<ClassType> callback : callbacks) {
            callback.accept(classType);
        }
    }

    public void addCallback(Consumer<ClassType> callback) {
        this.callbacks.add(callback);
    }

    public void generateDeclarations(ClassType declaringType, ClassType template) {
        if (declaringType.stage.isAfterOrAt(ResolutionStage.DECLARED)) {
            return;
        }
        if (template.stage.isBeforeOrAt(ResolutionStage.CREATED)) {
            throw new InternalException("Template declarations not generated yet");
        }
        var transformer = createTransformer(requireNonNull(declaringType.getTemplate()),
                ResolutionStage.DECLARED,
                declaringType.getTypeArguments(), typeFactory);
        transformer.enterClass(declaringType);
        template.getFields().forEach(transformer::transformField);
        template.getFlows().forEach(transformer::transformFlow);
        transformer.exitClass();
        declaringType.stage = ResolutionStage.DECLARED;
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
        if (declaringType.stage.isAfterOrAt(ResolutionStage.GENERATED)) {
            return;
        }
        if (template.stage.isBeforeOrAt(ResolutionStage.DECLARED)) {
            throw new InternalException("Template code not generated yet");
        }
        if (declaringType.stage.isBeforeOrAt(ResolutionStage.CREATED)) {
            generateDeclarations(declaringType, template);
        } else {
            var transformer = createTransformer(requireNonNull(declaringType.getTemplate()),
                    ResolutionStage.GENERATED,
                    declaringType.getTypeArguments(), typeFactory);
            transformer.enterClass(declaringType);
            NncUtils.biForEach(declaringType.getFlows(), template.getFlows(), (flow, flowTemplate) -> {
                if (flowTemplate.getRootScope() != null) {
                    transformer.enterFlow(flow);
                    flow.setRootScope(transformer.transformScope(flowTemplate.getRootScope()));
                    transformer.exitFlow();
                }
            });
            transformer.exitClass();
            declaringType.stage = ResolutionStage.GENERATED;
        }
    }

    public Flow getParameterizedFlow(Flow template, List<Type> typeArguments) {
        NncUtils.requireNull(template.getTemplate(), "Not a flow templateName");
        if (template.getTypeParameters().isEmpty()) {
            return template;
        }
        var ti = template.getTemplateInstance(typeArguments);
        if (ti != null) {
            return ti;
        }
        var transformer = createTransformer(template, ResolutionStage.GENERATED, typeArguments, typeFactory);
        transformer.enterClass(template.getDeclaringType());
        var transformed = transformer.transformFlow(template, template, typeArguments);
        transformer.exitClass();
        return transformed;
    }

    private boolean isVariableType(Type type) {
        return type instanceof TypeVariable;
    }


}
