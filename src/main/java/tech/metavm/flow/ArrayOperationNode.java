package tech.metavm.flow;

import org.jetbrains.annotations.Nullable;
import tech.metavm.dto.ErrorCode;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.BusinessException;

@EntityType("数组操作节点")
public abstract class ArrayOperationNode<P> extends NodeRT<P> {

    @ChildEntity("数组")
    private Value array;

    protected ArrayOperationNode(Long tmpId, String name, @Nullable Type outputType, NodeRT<?> previous, ScopeRT scope,
                                 Value array) {
        super(tmpId, name, outputType, previous, scope);
        setArray(array);
    }

    public Value getArray() {
        return array;
    }

    public void setArray(Value array) {
        if(!array.getType().isArray()) {
            throw new BusinessException(ErrorCode.NOT_AN_ARRAY_VALUE);
        }
        this.array = array;
    }

    public static ArrayType getArrayType(Value value) {
        if(value.getType() instanceof ArrayType arrayType) {
            return arrayType;
        }
        else {
            throw new BusinessException(ErrorCode.NOT_AN_ARRAY_VALUE);
        }
    }

    protected void checkTypes(Value array, Value element) {
        if (array.getType() instanceof ArrayType arrayType) {
            if (arrayType.getElementType().isAssignableFrom(element.getType())) {
                throw new BusinessException(ErrorCode.INCORRECT_ELEMENT_TYPE);
            }
        } else {
            throw new BusinessException(ErrorCode.NOT_AN_ARRAY_VALUE);
        }
    }

}
