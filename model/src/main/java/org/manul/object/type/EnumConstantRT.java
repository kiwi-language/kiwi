package org.manul.object.type;

import org.manul.object.instance.core.ClassInstance;
import org.manul.util.Instances;
import org.manul.util.InternalException;

public class EnumConstantRT {

    private final ClassInstance instance;

    public EnumConstantRT(ClassInstance instance) {
        if(!instance.getInstanceType().isEnum()) {
            throw new InternalException("Instance " + instance + " is not an enum instance");
        }
        this.instance = instance;
    }

    public ClassInstance getInstance() {
        return instance;
    }

    public Type getType() {
        return instance.getInstanceType();
    }

    public String getName() {
        return instance.getStringField(Types.getEnumNameField(instance.getInstanceKlass()));
    }

    public int getOrdinal() {
        return instance.getLongField(Types.getEnumOrdinalField(instance.getInstanceKlass())).getValue().intValue();
    }

    public void setName(String name) {
        instance.setField(Types.getEnumNameField(instance.getInstanceKlass()), Instances.stringInstance(name));
    }

    public Long getId() {
        return instance.tryGetTreeId();
    }

    public String getInstanceIdString() {
        return instance.getStringId();
    }

}
