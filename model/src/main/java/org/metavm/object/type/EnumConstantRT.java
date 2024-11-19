package org.metavm.object.type;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;

public class EnumConstantRT {

    private final ClassInstance instance;

    public EnumConstantRT(ClassInstance instance) {
        if(!instance.getType().isEnum()) {
            throw new InternalException("Instance " + instance + " is not an enum instance");
        }
        this.instance = instance;
    }

    public ClassInstance getInstance() {
        return instance;
    }

    public Type getType() {
        return instance.getType();
    }

    public String getName() {
        return instance.getStringField(Types.getEnumNameField(instance.getKlass())).getValue();
    }

    public int getOrdinal() {
        return instance.getLongField(Types.getEnumOrdinalField(instance.getKlass())).getValue().intValue();
    }

    public void setName(String name) {
        instance.setField(Types.getEnumNameField(instance.getKlass()), Instances.stringInstance(name));
    }

    public Long getId() {
        return instance.tryGetTreeId();
    }

    public String getInstanceIdString() {
        return instance.getStringId();
    }

}
