package org.metavm.entity.natives;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdField;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.StringValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Index;
import org.metavm.util.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class IndexNative extends NativeBase {

    private final ClassInstance instance;
    private transient Index index;

    public IndexNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Reference Index(Value name, Value unique, Value keyComputer, CallContext callContext) {
        instance.initField(StdField.indexMapName.get(), name);
        return instance.getReference();
    }

    public Value getFirst(Value key, CallContext callContext) {
        var indexKey = buildIndexKey(key, getIndex());
        return Objects.requireNonNullElseGet(
                callContext.instanceRepository().selectFirstByKey(indexKey),
                Instances::nullInstance
        );
    }

    public Value get(Value key, CallContext callContext) {
        var result = callContext.instanceRepository().indexSelect(buildIndexKey(key, getIndex()));
        return convertToList(result, callContext);
    }

    public Value query(Value min, Value max, CallContext callContext) {
        var result = callContext.instanceRepository().indexScan(
                buildIndexKey(min, getIndex()),
                buildIndexKey(max, getIndex())
        );
        return convertToList(result, callContext);
    }

    public Value count(Value min, Value max, CallContext callContext) {
        return Instances.longInstance(callContext.instanceRepository().indexCount(
                buildIndexKey(min, getIndex()),
                buildIndexKey(max, getIndex())
        ));
    }

    private Value convertToList(List<Reference> result, CallContext callContext) {
        var type = (ClassType) instance.getType().getTypeArguments().get(1);
        var listType = new ClassType(null, StdKlass.arrayList.get(), List.of(type));
        var list = ClassInstance.allocate(listType);
        var listNative = new ListNative(list);
        listNative.List(callContext);
        result.forEach(e -> listNative.add(e, callContext));
        return list.getReference();
    }

    private Index getIndex() {
        if (index == null) {
            var indexName = ((StringValue) instance.getField(StdField.indexMapName.get())).getValue();
            var klass = ((ClassType) instance.getType().getTypeArguments().get(1)).getKlass();
            index = Objects.requireNonNull(
                    klass.findIndex(idx -> idx.getName().equals(indexName)),
                    () -> "Cannot find index with name '" + indexName + "' in class " + klass
            );
        }
        return index;
    }

    private IndexKeyRT buildIndexKey(Value key, Index index) {
        if (key instanceof Reference ref) {
            if(ref.resolve() instanceof ClassInstance obj && obj.isValue()) {
                var values = new ArrayList<Value>();
                for (Field field : obj.getKlass().getFields()) {
                    values.add(obj.getField(field));
                }
                return index.createIndexKey(values);
            }
        }
        return index.createIndexKey(List.of(key));
    }

}
