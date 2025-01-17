package org.metavm.object.type.rest.dto;

import org.metavm.entity.GenericDeclarationRef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.List;

public record ParameterizedTypeKey(GenericDeclarationRefKey owner, Id templateId, List<TypeKey> typeArgumentKeys) implements TypeKey, GenericDeclarationRefKey {

    public static ParameterizedTypeKey create(@Nullable GenericDeclarationRef owner, Klass template, List<Type> typeArguments) {
        return new ParameterizedTypeKey(owner != null ? owner.toGenericDeclarationKey() : null,
                template.getId(), Utils.map(typeArguments, Type::toTypeKey));
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.PARAMETERIZED_TYPE);
        if(owner != null)
            owner.write(output);
        else
            output.write(WireTypes.NULL);
        output.writeId(templateId);
        output.writeInt(typeArgumentKeys.size());
        typeArgumentKeys.forEach(k -> k.write(output));
    }

    @Override
    public GenericDeclarationRef toGenericDeclarationRef(TypeDefProvider typeDefProvider) {
        return toType(typeDefProvider);
    }

    @Override
    public String toTypeExpression() {
        return "$$" + templateId + "<" + Utils.join(typeArgumentKeys, TypeKey::toTypeExpression) + ">";
    }

    @Override
    public ClassType toType(TypeDefProvider typeDefProvider) {
        return new KlassType(
                owner != null ? owner.toGenericDeclarationRef(typeDefProvider) : null,
                typeDefProvider.getKlass(templateId), Utils.map(typeArgumentKeys, k -> k.toType(typeDefProvider)));
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
