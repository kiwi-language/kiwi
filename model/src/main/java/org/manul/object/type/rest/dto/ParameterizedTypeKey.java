package org.manul.object.type.rest.dto;

import org.jsonk.Json;
import org.manul.entity.GenericDeclarationRef;
import org.manul.object.instance.core.Id;
import org.manul.object.type.*;
import org.manul.util.MvOutput;
import org.manul.util.Utils;
import org.manul.util.WireTypes;

import javax.annotation.Nullable;
import java.util.List;

@Json
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
    public String toTypeExpression() {
        return "$$" + templateId + "<" + Utils.join(typeArgumentKeys, TypeKey::toTypeExpression) + ">";
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitParameterizedTypeKey(this);
    }

    @Override
    public int getCode() {
        return WireTypes.PARAMETERIZED_TYPE;
    }

}
