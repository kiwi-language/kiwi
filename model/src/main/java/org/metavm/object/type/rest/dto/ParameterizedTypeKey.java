package org.metavm.object.type.rest.dto;

import org.metavm.entity.GenericDeclarationRef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;
import org.metavm.util.NncUtils;
import org.metavm.util.WireTypes;

import java.util.List;

public record ParameterizedTypeKey(Id templateId, List<TypeKey> typeArgumentKeys) implements TypeKey, GenericDeclarationRefKey {

    public static ParameterizedTypeKey create(Klass template, List<Type> typeArguments) {
        return new ParameterizedTypeKey(template.getId(), NncUtils.map(typeArguments, Type::toTypeKey));
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.PARAMETERIZED_TYPE);
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
        return WireTypes.PARAMETERIZED_TYPE;
    }

    @Override
    public GenericDeclarationRef resolve(TypeDefProvider typeDefProvider) {
        return toType(typeDefProvider);
    }
}
