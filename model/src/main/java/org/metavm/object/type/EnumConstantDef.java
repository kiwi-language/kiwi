package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.natives.CallContext;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.type.rest.dto.EnumConstantDefDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@EntityType
public class EnumConstantDef extends Element {

    public static final Logger logger = LoggerFactory.getLogger(EnumConstantDef.class);

    private final Klass klass;
    private String name;
    private int ordinal;
    private Method initializer;

    public EnumConstantDef(Klass klass, String name, int ordinal, Method initializer) {
        assert klass.isEnum();
        this.klass = klass;
        this.name = name;
        this.ordinal = ordinal;
        this.initializer = initializer;
        klass.addEnumConstantDef(this);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitEnumConstantDef(this);
    }

    public ClassInstance createEnumConstant(CallContext callContext) {
        return Objects.requireNonNull(Flows.invoke(initializer, null, List.of(), callContext)).resolveObject();
    }

    public Klass getKlass() {
        return klass;
    }

    public Field getField() {
        return klass.getStaticFieldByName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public EnumConstantDefDTO toDTO(SerializeContext serializeContext) {
        return new EnumConstantDefDTO(serializeContext.getStringId(this), name, ordinal, serializeContext.getStringId(initializer));
    }

    public Method getInitializer() {
        return initializer;
    }
}
