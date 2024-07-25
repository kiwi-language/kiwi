package org.metavm.object.type;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.ReadWriteArray;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.natives.CallContext;
import org.metavm.expression.EmptyEvaluationContext;
import org.metavm.flow.Flows;
import org.metavm.flow.Value;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.ClassInstanceBuilder;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.rest.dto.EnumConstantDefDTO;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EntityType
public class EnumConstantDef extends Element {

    public static final Logger logger = LoggerFactory.getLogger(EnumConstantDef.class);

    private final Klass klass;
    private String name;
    private int ordinal;
    @ChildEntity
    private final ReadWriteArray<Value> arguments = addChild(new ReadWriteArray<>(Value.class), "arguments");

    public EnumConstantDef(Klass klass, String name, int ordinal, List<Value> arguments) {
        assert klass.isEnum();
        this.klass = klass;
        this.name = name;
        this.ordinal = ordinal;
        this.arguments.addAll(arguments);
        klass.addEnumConstantDef(this);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitEnumConstantDef(this);
    }

    public ClassInstance createEnumConstant(CallContext callContext) {
        var arguments = new ArrayList<Instance>();
        arguments.add(Instances.stringInstance(name));
        arguments.add(Instances.longInstance(ordinal));
        var evalCtx = new EmptyEvaluationContext();
        for (Value arg : this.arguments) {
            arguments.add(arg.evaluate(evalCtx));
        }
        var constructor = klass.resolveMethod(
                klass.getName(),
                NncUtils.map(arguments, Instance::getType),
                List.of(),
                false
        );
        var self = ClassInstanceBuilder.newBuilder(klass.getType()).build();
        return Objects.requireNonNull(Flows.invoke(constructor, self, arguments, callContext)).resolveObject();
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

    public List<Value> getArguments() {
        return arguments.toList();
    }

    public void setArguments(List<Value> arguments) {
        this.arguments.reset(arguments);
    }

    public EnumConstantDefDTO toDTO(SerializeContext serializeContext) {
        return new EnumConstantDefDTO(
                serializeContext.getStringId(this),
                name,
                ordinal,
                NncUtils.map(arguments, Value::toDTO)
        );
    }

}
