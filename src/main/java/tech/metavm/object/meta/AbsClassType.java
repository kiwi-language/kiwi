package tech.metavm.object.meta;

import tech.metavm.flow.FlowRT;
import tech.metavm.util.Column;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.List;
import java.util.Objects;

import static tech.metavm.util.NncUtils.find;

public abstract class AbsClassType extends Type {

    public AbsClassType(String name, boolean anonymous, boolean ephemeral, TypeCategory category) {
        super(name, anonymous, ephemeral, category);
    }

    public abstract List<Field> getFields();

    public Field getFieldNyNameRequired(String name){
        return NncUtils.findRequired(getFields(), f -> f.getName().equals(name));
    }

    public Field getField(long fieldId) {
        return NncUtils.findRequired(getFields(), f -> f.getId() == fieldId);
    }

    public UniqueConstraintRT getUniqueConstraint(long constraintId) {
        return NncUtils.findRequired(
                getConstraints(UniqueConstraintRT.class),
                c -> Objects.equals(c.getId(), constraintId)
        );
    }

    public UniqueConstraintRT getUniqueConstraint(List<Field> fields) {
        return find(getUniqueConstraints(), c -> c.getFields().equals(fields));
    }

    public List<UniqueConstraintRT> getUniqueConstraints() {
        return getConstraints(UniqueConstraintRT.class);
    }

    public abstract void addConstraint(ConstraintRT<?> constraint);

    public abstract void removeConstraint(long id);

    public Field getFieldByJavaField(java.lang.reflect.Field javaField) {
        String fieldName = ReflectUtils.getMetaFieldName(javaField);
        return NncUtils.requireNonNull(getFieldByName(fieldName),
                "Can not find field for java field " + javaField);
    }

    public abstract List<FlowRT> getFlows();

    public FlowRT getFlow(long flowId) {
        return NncUtils.findRequired(getFlows(), flow -> Objects.equals(flow.getId(), flowId));
    }

    public boolean containsField(long fieldId) {
        return NncUtils.find(getFields(), f -> Objects.equals(f.getId(), fieldId)) != null;
    }

    public Field getFieldByName(String fieldName) {
        return NncUtils.find(getFields(), f -> Objects.equals(f.getName(), fieldName));
    }


    public List<ConstraintRT<?>> getConstraints() {
        return List.of();
    }

    public <T extends ConstraintRT<?>> List<T> getConstraints(Class<T> constraintType) {
        return NncUtils.filterByType(getConstraints(), constraintType);
    }

    public Field getTileField() {
        return NncUtils.find(getFields(), Field::isAsTitle);
    }


    public abstract AbsClassType getSuperType();

    @Override
    public boolean isAssignableFrom(Type that) {
        return false;
    }

    @Override
    protected Object getParam() {
        return null;
    }

    abstract Column allocateColumn(Field field);

    public abstract void addField(Field field);

    abstract void removeField(Field field);

}
