package org.metavm.entity.natives;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdField;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.Instances;

import java.util.List;
import java.util.Objects;

@Slf4j
public class IndexNative extends NativeBase {

    private final ClassInstance instance;
    private transient IndexRef index;

    public IndexNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Value Index(Value name, Value unique, Value keyComputer, CallContext callContext) {
        instance.initField(StdField.indexName.get(), name);
        return instance.getReference();
    }

    public Value getFirst(Value key, CallContext callContext) {
        var indexKey = buildIndexKey(getIndex(), key);
        return Objects.requireNonNullElseGet(
                callContext.instanceRepository().selectFirstByKey(indexKey),
                Instances::nullInstance
        );
    }

    public Value get(Value key, CallContext callContext) {
        var result = callContext.instanceRepository().indexSelect(buildIndexKey(getIndex(), key));
        return convertToList(result, callContext);
    }

    public Value query(Value min, Value max, CallContext callContext) {
        var result = callContext.instanceRepository().indexScan(
                buildIndexKey(getIndex(), min),
                buildIndexKey(getIndex(), max)
        );
        return convertToList(result, callContext);
    }

    public Value count(Value min, Value max, CallContext callContext) {
        return Instances.longInstance(callContext.instanceRepository().indexCount(
                buildIndexKey(getIndex(), min),
                buildIndexKey(getIndex(), max)
        ));
    }

    private Value convertToList(List<Reference> result, CallContext callContext) {
        var type = (ClassType) instance.getInstanceType().getTypeArguments().get(1);
        var listType = new KlassType(null, StdKlass.arrayList.get(), List.of(type));
        var list = ClassInstance.allocate(listType);
        var listNative = new ListNative(list);
        listNative.List(callContext);
        result.forEach(e -> listNative.add(e, callContext));
        return list.getReference();
    }

    private IndexRef getIndex() {
        if (index == null) {
            var indexName = Instances.toJavaString(instance.getField(StdField.indexName.get()));
            var valueType = (ClassType) instance.getInstanceType().getTypeArguments().get(1);
            index = Objects.requireNonNull(
                    valueType.findSelfIndex(idx -> idx.getName().equals(indexName)),
                    () -> "Cannot find index with name '" + indexName + "' in class " + valueType
            );
        }
        return index;
    }

    private IndexKeyRT buildIndexKey(IndexRef index, Value key) {
        return index.getRawIndex().createIndexKey(Indexes.getIndexValues(index, key));
    }

}
