package org.metavm.entity.natives;

import org.metavm.common.ErrorCode;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Field;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SetNative extends IterableNative {

    private final ClassInstance instance;
    private final Map<HashKeyWrap, Integer> element2index = new HashMap<>();
    private final Field arrayField;
    private ArrayInstance array;

    public SetNative(ClassInstance instance) {
        this.instance = instance;
        arrayField = NncUtils.requireNonNull(instance.getKlass().findFieldByCode("array"));
        if (instance.isFieldInitialized(arrayField)) {
            var instCtx = Objects.requireNonNull(instance.getContext(), "InstanceContext is missing in " + instance);
            array = instance.getField(arrayField).resolveArray();
            initializeElementToIndex(instCtx);
        }
    }

    public Value HashSet(CallContext callContext) {
        array = new ArrayInstance((ArrayType) arrayField.getType());
        instance.initField(arrayField, array.getReference());
        return instance.getReference();
    }

    public Value HashSet(Value c, CallContext callContext) {
        if(c instanceof Reference collection) {
            var thatArrayField = collection.resolveObject().getKlass().getFieldByCode("array");
            var thatArray = collection.resolveObject().getField(thatArrayField).resolveArray();
            array = new ArrayInstance((ArrayType) arrayField.getType(),
                    new InstanceParentRef(instance.getReference(), arrayField));
            instance.initField(arrayField, array.getReference());
            array.addAll(thatArray);
            initializeElementToIndex(callContext);
            return instance.getReference();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public LongValue hashCode(CallContext callContext) {
        int h = 0;
        for (Value value : array) {
            h = h + Instances.hashCode(value, callContext);
        }
        return Instances.longInstance(h);
    }

    public BooleanValue equals(Value o, CallContext callContext) {
        if(o instanceof Reference ref) {
            if(ref.resolve() instanceof ClassInstance that
                    && that.getKlass().findAncestorKlassByTemplate(StdKlass.set.get()) == instance.getKlass().findAncestorKlassByTemplate(StdKlass.set.get())) {
                var thatNat = new SetNative(that);
                var thatArray = thatNat.array;
                if(array.size() == thatArray.size()) {
                    for (Value value : thatArray) {
                        if(!element2index.containsKey(new HashKeyWrap(value, callContext)))
                            return Instances.falseInstance();
                    }
                    return Instances.trueInstance();
                }
            }
        }
        return Instances.falseInstance();
    }

    public Reference iterator(CallContext callContext) {
        var iteratorImplType = StdKlass.iteratorImpl.get().getParameterized(List.of(instance.getKlass().getFirstTypeArgument()));
        var it = ClassInstance.allocate(iteratorImplType.getType());
        var itNative = (IteratorImplNative) NativeMethods.getNativeObject(it);
        itNative.IteratorImpl(instance, callContext);
        return it.getReference();
    }

    public Value add(Value value, CallContext callContext) {
        var keyWrap = new HashKeyWrap(value, callContext);
        if (!element2index.containsKey(keyWrap)) {
            element2index.put(keyWrap, array.size());
            array.addElement(value);
            return Instances.trueInstance();
        } else {
            return Instances.falseInstance();
        }
    }

    public Value remove(Value value, CallContext callContext) {
        var keyWrap = new HashKeyWrap(value, callContext);
        Integer index = element2index.remove(keyWrap);
        if (index != null) {
            int lastIdx = array.size() - 1;
            var last = array.removeElement(lastIdx);
            if (index != lastIdx) {
                array.setElement(index, last);
                element2index.put(new HashKeyWrap(last, callContext), index);
            }
            return Instances.trueInstance();
        } else {
            return Instances.falseInstance();
        }
    }

    public Value isEmpty(CallContext callContext) {
        return Instances.booleanInstance(element2index.isEmpty());
    }

    public Value contains(Value value, CallContext callContext) {
        return Instances.booleanInstance(element2index.containsKey(new HashKeyWrap(value, callContext)));
    }

    public Value size(CallContext callContext) {
        return Instances.longInstance(element2index.size());
    }

    public void clear(CallContext callContext) {
        array.clear();
        element2index.clear();
    }

    @Override
    public void forEach(Value action, CallContext callContext) {
        if(action instanceof FunctionValue functionValue)
            array.forEach(e -> functionValue.execute(List.of(e), callContext));
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    private void initializeElementToIndex(CallContext callContext) {
        for (int i = 0; i < array.getElements().size(); i++) {
            element2index.put(new HashKeyWrap(array.get(i), callContext), i);
        }
    }

}
