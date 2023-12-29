package tech.metavm.entity;

import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EntityDesc {

    private final Class<?> klass;
    private final List<EntityProp> props = new ArrayList<>();

    public EntityDesc(Class<?> klass) {
        this.klass = klass;
        List<Field> fields = ReflectionUtils.getInstanceFields(klass);
        for (Field field : fields) {
            props.add(new EntityProp(field));
        }
    }

    public List<Entity> getRelatedEntities(Entity object, EntityDesc desc) {
        if(object == null) {
            return List.of();
        }
        List<Entity> result = new ArrayList<>();
        for (EntityProp prop : desc.getProps()) {
            if(prop.isNull(object)) {
                continue;
            }
            if(prop.isEntity()) {
                result.add(prop.getEntity(object));
            }
            else if(prop.isEntityList()) {
                result.addAll(prop.getEntityList(object));
            }
        }
        return result;
    }

    public List<EntityProp> getProps() {
        return props;
    }

    public List<EntityProp> getNonTransientProps() {
        return NncUtils.exclude(props, EntityProp::isTransient);
    }

    public List<EntityProp> getPropsWithAnnotation(Class<? extends Annotation> annotationClass) {
        return NncUtils.filter(props, p -> p.getField().isAnnotationPresent(annotationClass));
    }

    public Class<?> getKlass() {
        return klass;
    }


    @Override
    public String toString() {
        return klass.getName();
    }
}
