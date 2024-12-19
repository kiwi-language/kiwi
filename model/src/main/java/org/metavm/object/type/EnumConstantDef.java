package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.StdField;
import org.metavm.flow.KlassInput;
import org.metavm.flow.KlassOutput;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.util.Instances;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class EnumConstantDef extends Element implements ITypeDef {

    public static final Logger logger = LoggerFactory.getLogger(EnumConstantDef.class);

    private Klass klass;
    private String name;
    private int ordinal;

    public EnumConstantDef(Klass klass, String name, int ordinal, Method initializer) {
//        assert klass.isEnum();
        this.klass = klass;
        this.name = name;
        this.ordinal = ordinal;
        klass.addEnumConstantDef(this);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitEnumConstantDef(this);
    }

    public Klass getKlass() {
        return klass;
    }

    public void setKlass(Klass klass) {
        this.klass = klass;
    }

    public Field getField() {
        return klass.getSelfStaticFieldByName(name);
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

    public void write(KlassOutput output) {
        output.writeEntityId(this);
        output.writeUTF(name);
        output.writeInt(ordinal);
    }

    public void read(KlassInput input) {
        name = input.readUTF();
        ordinal = input.readInt();
    }

    public void update(IEntityContext context) {
        var value = getValue(context);
        value.setField(StdField.enumName.get(), Instances.stringInstance(name));
        value.setField(StdField.enumOrdinal.get(), Instances.intInstance(ordinal));
//        logger.debug("Setting ordinal of enum constant {} to {}", name, ordinal);
    }

    public ClassInstance getValue(IEntityContext context) {
        var sft = StaticFieldTable.getInstance(klass.getType(), context);
        return sft.get(getField()).resolveObject();
    }

}
