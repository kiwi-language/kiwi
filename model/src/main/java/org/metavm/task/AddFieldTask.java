package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.flow.Flows;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;

import java.util.List;
import java.util.Objects;

@EntityType
public class AddFieldTask extends ScanByClassTask {

    private final Field field;

    protected AddFieldTask(Klass declaringType, Field field) {
        super(String.format("Add field task %s.%s", declaringType.getName(), field.getName()), declaringType.getType());
        this.field = field;
    }

    @Override
    protected void processClassInstance(ClassInstance instance, IEntityContext context) {
        Instance fieldValue;
        if(field.isChild() && field.getType() instanceof ArrayType arrayType)
            fieldValue = new ArrayInstance(arrayType);
        else
            fieldValue = computeFieldInitialValue(instance, context);
        if(!instance.isFieldInitialized(field))
            instance.initField(field, fieldValue);
    }

    private Instance computeFieldInitialValue(ClassInstance instance, IEntityContext context) {
        var klass = field.getDeclaringType();
        var initMethodName = "__" + field.getCodeNotNull() + "__";
        var initMethod = klass.findMethodByCodeAndParamTypes(initMethodName, List.of());
        if(initMethod != null)
            return Flows.invoke(initMethod, instance, List.of(), context);
        else
            return Objects.requireNonNull(field.getDefaultValue());
    }

    public Field getField() {
        return field;
    }
}
