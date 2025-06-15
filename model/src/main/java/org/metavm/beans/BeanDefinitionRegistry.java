package org.metavm.beans;

import lombok.extern.slf4j.Slf4j;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.entity.StdKlass;
import org.metavm.flow.Flow;
import org.metavm.flow.Method;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassType;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

@NativeEntity(53)
@Entity
@Slf4j
public class BeanDefinitionRegistry extends org.metavm.entity.Entity implements Message {
    public static final IndexDef<BeanDefinitionRegistry> IDX_ALL_FLAGS = IndexDef.create(BeanDefinitionRegistry.class,
            1, bdr -> List.of(Instances.booleanInstance(bdr.allFlags))
            );
    @SuppressWarnings("unused")
    private static Klass __klass__;

    @SuppressWarnings("unused")
    private boolean allFlags = true;

    private transient List<BeanDefinition> interceptorDefinitions = new ArrayList<>();

    private List<BeanDefinition> beanDefinitions = new ArrayList<>();

    public static BeanDefinitionRegistry getInstance(IInstanceContext context) {
        return Objects.requireNonNull(context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance()),
                "BeanDefinitionRegistry not found in context " + context.getAppId());
    }

    public static void initialize(IInstanceContext context) {
        var existing = context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance());
        if(existing != null)
            throw new IllegalStateException("BeanDefinitionRegistry already exists");
        context.bind(new BeanDefinitionRegistry(context.allocateRootId()));
    }

    private BeanDefinitionRegistry(Id id) {
        super(id);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitBoolean();
        visitor.visitList(visitor::visitEntity);
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    public void registerBeanDefinition(BeanDefinition beanDefinition) {
        if(Utils.exists(beanDefinitions, b -> b.getName().equals(beanDefinition.getName())))
            throw new IllegalStateException("BeanDefinition with name " + beanDefinition.getName() + " already exists");
        addBeanDefinition(beanDefinition);
    }

    private void addBeanDefinition(BeanDefinition beanDefinition) {
        beanDefinitions.add(beanDefinition);
        if(isInterceptor(beanDefinition.getBeanType()))
            interceptorDefinitions.add(beanDefinition);
    }

    public List<BeanDefinition> removeBeanDefByType(ClassType type) {
        var it = beanDefinitions.iterator();
        var removed = new ArrayList<BeanDefinition>();
        while (it.hasNext()) {
            var bd = it.next();
            if (type.isAssignableFrom(bd.getBeanType())) {
                removed.add(bd);
                it.remove();
            }
        }
        if (isInterceptor(type))
            interceptorDefinitions.removeIf(bd -> type.isAssignableFrom(bd.getBeanType()));
        return removed;
    }

    public List<BeanDefinition> removeBeanDefByFactoryMethod(Method method) {
        var it = beanDefinitions.iterator();
        var removed = new ArrayList<BeanDefinition>();
        while (it.hasNext()) {
            var bd = it.next();
            if (isFactoryBeanDef(bd, method)) {
                removed.add(bd);
                it.remove();
            }
        }
        if (method.getReturnType() instanceof ClassType ct && isInterceptor(ct))
            interceptorDefinitions.removeIf(bd -> isFactoryBeanDef(bd, method));
        return removed;
    }

    private boolean isFactoryBeanDef(BeanDefinition beanDef, Method method) {
        return beanDef instanceof FactoryBeanDefinition fbd && fbd.getMethod() == method;
    }

    private boolean isInterceptor(ClassType classType) {
        return StdKlass.interceptor.type().isAssignableFrom(classType);
    }

    public @Nullable BeanDefinition tryGetBeanDefinition(String name) {
        return Utils.find(beanDefinitions, b -> b.getName().equals(name));
    }

    public BeanDefinition getBeanDefinition(String name) {
        return Utils.findRequired(beanDefinitions, b -> b.getName().equals(name),
                "BeanDefinition with name " + name + " not found");
    }

    public ClassInstance getBean(String name) {
        return getBeanDefinition(name).resolveBean();
    }

    public @Nullable ClassInstance tryGetBean(String name) {
        var def = tryGetBeanDefinition(name);
        return def != null ? def.resolveBean() : null;
    }

    public void removeBeanDefinition(String name) {
        var beanDef = Utils.findRequired(beanDefinitions, bean -> bean.getName().equals(name),
                "BeanDefinition with name " + name + " not found");
        beanDefinitions.remove(beanDef);
        if(StdKlass.interceptor.type().isAssignableFrom(beanDef.getBeanType()))
            interceptorDefinitions.remove(beanDef);
    }

    public List<BeanDefinition> getBeanDefinitionsByType(ClassType type) {
        return Utils.filter(beanDefinitions, b -> type.isAssignableFrom(b.getBeanType()));
    }

    public List<ClassInstance> getBeansOfType(ClassType type) {
        return Utils.filterAndMap(beanDefinitions, b -> type.isAssignableFrom(b.getBeanType()), BeanDefinition::resolveBean);
    }

    public List<Value> getFlowArguments(Flow method) {
        var arguments = new ArrayList<Value>();
        for (Parameter parameter : method.getParameters()) {
            var beanName = parameter.getAttribute(AttributeNames.BEAN_NAME);
            if (beanName != null) {
                var bean = getBean(beanName);
                if (parameter.getType().isInstance(bean.getReference())) {
                    arguments.add(bean.getReference());
                } else {
                    throw new InternalException("Type mismatch for named bean '" + beanName + "': expected '" + parameter.getType() + "'.");
                }
                continue;
            }
            if (parameter.getType().getUnderlyingType() instanceof KlassType paramType) {
                if (paramType.isList()) {
                    if (paramType.getFirstTypeArgument() instanceof KlassType beanType) {
                        var matchingBeans = getBeansOfType(beanType);
                        var beanReferences = Utils.map(matchingBeans, Instance::getReference);
                        arguments.add(Instances.createList(paramType, beanReferences).getReference());
                    } else {
                        throw new InternalException("Cannot autowire List: unsupported element type '" + paramType.getFirstTypeArgument() + "'.");
                    }
                } else {
                    var beans = getBeansOfType(paramType);
                    if (beans.isEmpty()) {
                        throw new InternalException("No qualifying bean of type '" + paramType + "' found.");
                    }
                    if (beans.size() > 1) {
                        throw new InternalException("Expected 1 bean of type '" + paramType + "', but found " + beans.size() + ".");
                    }
                    arguments.add(beans.getFirst().getReference());
                }
            } else {
                throw new InternalException("Cannot resolve parameter: unsupported type '" + parameter.getType() + "' in method '" + method.getName() + "'.");
            }
        }
        return arguments;
    }
    public List<ClassInstance> getInterceptors() {
        return Utils.map(interceptorDefinitions, BeanDefinition::resolveBean);
    }

    public List<BeanDefinition> getFlowDependencies(Flow flow) {
        return Utils.flatMap(flow.getParameters(), parameter -> {
            var beanName = parameter.getAttribute(AttributeNames.BEAN_NAME);
            if (beanName != null)
                return List.of(getBeanDefinition(beanName));
            var type = parameter.getType().getUnderlyingType();
            if(type instanceof KlassType classType) {
                if (classType.isList()) {
                    if(classType.getFirstTypeArgument() instanceof KlassType elementType)
                        return getBeanDefinitionsByType(elementType);
                    else
                        return List.of();
                }
                return getBeanDefinitionsByType(classType);
            }
            else
                return List.of();
        });

    }

    private void onRead() {
        interceptorDefinitions = new ArrayList<>();
        for (var beanDefinition : beanDefinitions) {
            if(StdKlass.interceptor.type().isAssignableFrom(beanDefinition.getBeanType()))
                interceptorDefinitions.add(beanDefinition);
        }
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        for (var beanDefinitions_ : beanDefinitions) action.accept(beanDefinitions_.getReference());
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("interceptors", this.getInterceptors());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        for (var beanDefinitions_ : beanDefinitions) action.accept(beanDefinitions_);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_BeanDefinitionRegistry;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.allFlags = input.readBoolean();
        this.beanDefinitions = input.readList(() -> input.readEntity(BeanDefinition.class, this));
        this.onRead();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeBoolean(allFlags);
        output.writeList(beanDefinitions, output::writeEntity);
    }

    public List<BeanDefinition> getBeanDefinitions() {
        return Collections.unmodifiableList(beanDefinitions);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
    }
}
