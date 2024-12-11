package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.entity.Attribute;
import org.metavm.entity.Element;
import org.metavm.entity.ReadWriteArray;
import org.metavm.flow.ErrorLevel;
import org.metavm.flow.Method;

import java.util.List;

@Slf4j
@Entity(ephemeral = true)
public class DummyKlass extends Klass {

    public static final DummyKlass INSTANCE = new DummyKlass();

    private DummyKlass() {
        super(null,
                "<Dummy>",
                "<Dummy>",
                null,
                List.of(),
                ClassKind.CLASS,
                ClassSource.BUILTIN,
                false,
                false,
                false,
                false,
                null,
                false,
                false,
                null,
                null,
                List.of(),
                0,
                0,
                0);
    }

    @Override
    public void addMethod(Method method) {
    }

    @Override
    protected void addExtension(Klass klass) {
    }

    @Override
    protected void addImplementation(Klass klass) {
    }

    @Override
    public void addConstraint(Constraint constraint) {
    }

    @Override
    public void addEnumConstantDef(EnumConstantDef enumConstantDef) {
    }

    @Override
    public void addError(Element element, ErrorLevel level, String message) {
    }

    @Override
    public void addField(Field field) {
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
    }

    public ReadWriteArray<Attribute> getAttributeArray() {
        return attributes;
    }

    @Override
    public ConstantPool getConstantPool() {
        return new ConstantPool() {
            @Override
            public int addValue(Object value) {
                return 0;
            }
        };
    }
}
