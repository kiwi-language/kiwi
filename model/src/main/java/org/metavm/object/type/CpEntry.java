package org.metavm.object.type;

import org.metavm.api.ValueObject;
import org.metavm.entity.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.FunctionRef;
import org.metavm.flow.LambdaRef;
import org.metavm.flow.MethodRef;
import org.metavm.object.type.rest.dto.*;
import org.metavm.object.view.ObjectMappingRef;
import org.metavm.util.Instances;

public abstract class CpEntry extends Entity implements ValueObject {

    public static CpEntry fromDTO(CpEntryDTO cpEntryDTO, IEntityContext context) {
        return switch (cpEntryDTO) {
            case ValueCpEntryDTO valueCpEntryDTO -> new ValueCpEntry(
                    valueCpEntryDTO.index(),
                    Instances.fromFieldValue(valueCpEntryDTO.value(), id -> context.getInstanceContext().get(id).getReference())
            );
            case TypeCpEntryDTO typeCpEntryDTO -> new TypeCpEntry(typeCpEntryDTO.index(),
                    TypeParser.parseType(typeCpEntryDTO.type(), context));
            case FieldCpEntryDTO fieldCpEntryDTO ->
                    new FieldCpEntry(fieldCpEntryDTO.index(), FieldRef.create(fieldCpEntryDTO.fieldRef(), context));
            case MethodCpEntryDTO methodCpEntryDTO ->
                new MethodCpEntry(methodCpEntryDTO.index(), MethodRef.createMethodRef(methodCpEntryDTO.methodRef(), context));
            case FunctionCpEntryDTO functionCpEntryDTO ->
                new FunctionCpEntry(functionCpEntryDTO.index(), FunctionRef.create(functionCpEntryDTO.functionRef(), context));
            case MappingCpEntryDTO mappingCpEntryDTO ->
                new MappingCpEntry(mappingCpEntryDTO.index(), ObjectMappingRef.create(mappingCpEntryDTO.mappingRef(), context));
            case IndexCpEntryDTO indexCpEntryDTO ->
                new IndexCpEntry(indexCpEntryDTO.index(), IndexRef.create(indexCpEntryDTO.indexRef(), context));
            case LambdaCpEntryDTO lambdaCpEntryDTO ->
                new LambdaCpEntry(lambdaCpEntryDTO.index(), LambdaRef.fromDTO(lambdaCpEntryDTO.lambdaRef(), context));
            default -> throw new IllegalStateException("Unrecognized CpEntryDTO: " + cpEntryDTO);
        };
    }

    private final int index;

    protected CpEntry(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public abstract Object getValue();

    public abstract Object resolve();

    public abstract CpEntryDTO toDTO(SerializeContext serializeContext);

}
