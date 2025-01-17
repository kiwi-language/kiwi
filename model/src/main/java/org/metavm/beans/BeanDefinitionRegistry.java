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
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassType;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        return Objects.requireNonNull(context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance()), "BeanDefinitionRegistry not found");
    }

    public static void initialize(IInstanceContext context) {
        var existing = context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance());
        if(existing != null)
            throw new IllegalStateException("BeanDefinitionRegistry already exists");
        context.bind(new BeanDefinitionRegistry());
    }

    private BeanDefinitionRegistry() {
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
        if(StdKlass.interceptor.type().isAssignableFrom(beanDefinition.getBeanType()))
            interceptorDefinitions.add(beanDefinition);
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
                if (parameter.getType().isInstance(bean.getReference()))
                    arguments.add(bean.getReference());
                else
                    throw new InternalException("Bean " + beanName + " is not of type " + parameter.getType());
                continue;
            }
            if (parameter.getType().getUnderlyingType() instanceof KlassType paramType) {
                if (paramType.isList()) {
                    if(paramType.getFirstTypeArgument() instanceof KlassType beanType)
                        arguments.add(Instances.createList(paramType, Utils.map(getBeansOfType(beanType), Instance::getReference)).getReference());
                    else
                        throw new InternalException("Unsupported list element type " + paramType.getFirstTypeArgument() + " in bean factory method " + method.getName());
                } else {
                    var beans = getBeansOfType(paramType);
                    if (beans.isEmpty())
                        throw new InternalException("No beans of type " + paramType + " found");
                    if (beans.size() > 1)
                        throw new InternalException("Multiple beans of type " + paramType + " found");
                    arguments.add(beans.getFirst().getReference());
                }
            } else {
                throw new InternalException("Unsupported parameter type " + parameter.getType() + " in bean factory method " + method.getName());
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
        beanDefinitions.forEach(arg -> action.accept(arg.getReference()));
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
        beanDefinitions.forEach(action);
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

    @Override
    protected void buildSource(Map<String, Value> source) {
    }
}
