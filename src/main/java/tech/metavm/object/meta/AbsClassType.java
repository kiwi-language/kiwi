package tech.metavm.object.meta;

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

    public Index getUniqueConstraint(long constraintId) {
        return NncUtils.findRequired(
                getConstraints(Index.class),
                c -> Objects.equals(c.getId(), constraintId)
        );
    }

    public Index getUniqueConstraint(List<Field> fields) {
        return find(getUniqueConstraints(), c -> c.getTypeFields().equals(fields));
    }

    public List<Index> getUniqueConstraints() {
        return getConstraints(Index.class);
    }

    public abstract void addConstraint(Constraint<?> constraint);

    public Field getFieldByJavaField(java.lang.reflect.Field javaField) {
        String fieldName = ReflectUtils.getMetaFieldName(javaField);
        return NncUtils.requireNonNull(getFieldByName(fieldName),
                "Can not find indexItem for java indexItem " + javaField);
    }

    public boolean containsField(long fieldId) {
        return NncUtils.find(getFields(), f -> Objects.equals(f.getId(), fieldId)) != null;
    }

    public Field getFieldByName(String fieldName) {
        return NncUtils.find(getFields(), f -> Objects.equals(f.getName(), fieldName));
    }


    public List<Constraint<?>> getConstraints() {
        return List.of();
    }

    public <T extends Constraint<?>> List<T> getConstraints(Class<T> constraintType) {
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
