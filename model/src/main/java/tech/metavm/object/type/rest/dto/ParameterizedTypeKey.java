package tech.metavm.object.type.rest.dto;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;

import java.util.List;

public record ParameterizedTypeKey(Id templateId, List<TypeKey> typeArgumentKeys) implements TypeKey{

    public static ParameterizedTypeKey create(Klass template, List<Type> typeArguments) {
        return new ParameterizedTypeKey(template.getId(), NncUtils.map(typeArguments, Type::toTypeKey));
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.PARAMETERIZED);
        output.writeId(templateId);
        output.writeInt(typeArgumentKeys.size());
        typeArgumentKeys.forEach(k -> k.write(output));
    }

    @Override
    public String toTypeExpression() {
        return "$$" + templateId + "<" + NncUtils.join(typeArgumentKeys, TypeKey::toTypeExpression) + ">";
    }

    @Override
    public ClassType toType(TypeDefProvider typeDefProvider) {
        return new ClassType(typeDefProvider.getKlass(templateId), NncUtils.map(typeArgumentKeys, k -> k.toType(typeDefProvider)));
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitParameterizedTypeKey(this);
    }

    @Override
    public int getCode() {
        return TypeKeyCodes.PARAMETERIZED;
    }

}
