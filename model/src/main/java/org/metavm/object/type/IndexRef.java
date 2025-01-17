package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.Entity;
import org.metavm.entity.Writable;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.InstanceEvaluationContext;
import org.metavm.flow.Flows;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.util.*;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@Slf4j
public class IndexRef implements org.metavm.entity.Reference, Writable {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final ClassType declaringType;
    private final Reference indexReference;
//    public Index rawIndex;

    public IndexRef(ClassType declaringType, Index rawIndex) {
        this(declaringType, rawIndex.getReference());
//        this.rawIndex = rawIndex;
    }

    private IndexRef(ClassType declaringType, Reference indexReference) {
        this.declaringType = declaringType;
        this.indexReference = indexReference;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IndexRef that && that.declaringType.equals(declaringType) && that.indexReference.equals(indexReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringType, indexReference);
    }

    public void write(MvOutput output) {
        output.write(WireTypes.INDEX_REF);
        declaringType.write(output);
        output.writeReference(indexReference);
    }

    public static IndexRef read(MvInput input) {
        var classType = (ClassType) input.readType();
        var reference = input.readReference();
        return new IndexRef(classType, reference);
    }

    public Index getRawIndex() {
        return (Index) indexReference.get();
    }

    public String getName() {
        return getRawIndex().getName();
    }

    public int getFieldCount() {
        return getRawIndex().getNumFields();
    }

    public List<Type> getIndexFieldTypes() {
        return Utils.map(getRawIndex().getFields(), f -> f.getType(declaringType.getTypeMetadata()));
    }

    public ClassType getDeclaringType() {
        return declaringType;
    }

    public List<IndexKeyRT> createIndexKey(ClassInstance instance) {
        var result = new ArrayList<IndexKeyRT>();
        forEachIndexKey(instance, result::add);
        return result;
    }

    public void forEachIndexKey(Instance instance, Consumer<IndexKeyRT> action) {
        if (instance instanceof MvClassInstance clsInst) {
            EvaluationContext evaluationContext = new InstanceEvaluationContext(clsInst);
            Map<IndexField, Value> values = new HashMap<>();
            var method = getRawIndex().getMethod();
            var fields = getRawIndex().getFields();
            if (method != null) {
                var entityContext = ContextUtil.getEntityContext();
                var indexValues =
                        method.isStatic() ?
                                Indexes.getIndexValues(this, requireNonNull(Flows.execute(method.getRef(), null, List.of(clsInst.getReference()), entityContext).ret())) :
                                Indexes.getIndexValues(this, requireNonNull(Flows.execute(declaringType.getMethod(method), clsInst, List.of(), entityContext).ret()));
                var lastIdx = fields.size() - 1;
                for (int i = 0; i < lastIdx; i++) {
                    var field = fields.get(i);
                    values.put(field, indexValues.get(i));
                }
                // When the last index item is an array, create an index key for each element.
                var lastField = fields.get(lastIdx);
                if (Objects.requireNonNull(lastField.getValue()).getType().getUnderlyingType().isArray()) {
                    var lastValues = new HashSet<>(indexValues.get(lastIdx).resolveArray().getElements());
                    for (Value lastValue : lastValues) {
                        values.put(lastField, lastValue);
                        action.accept(new IndexKeyRT(getRawIndex(), values));
                    }
                } else {
                    values.put(lastField, indexValues.get(lastIdx));
                    action.accept(new IndexKeyRT(getRawIndex(), values));
                }
            } else {
                for (int i = 0; i < fields.size() - 1; i++) {
                    var field = fields.get(i);
                    values.put(field, Objects.requireNonNull(field.getValue()).evaluate(evaluationContext));
                }
                // When the last index item is an array, create an index key for each element.
                var lastField = fields.get(fields.size() - 1);
                if (Objects.requireNonNull(lastField.getValue()).getType().getUnderlyingType().isArray()) {
                    var lastValues = new HashSet<>((lastField.getValue().evaluate(evaluationContext)).resolveArray().getElements());
                    List<IndexKeyRT> keys = new ArrayList<>();
                    for (Value lastValue : lastValues) {
                        values.put(lastField, lastValue);
                        action.accept(new IndexKeyRT(getRawIndex(), values));
                    }
                } else {
                    values.put(lastField, lastField.getValue().evaluate(evaluationContext));
                    action.accept(new IndexKeyRT(getRawIndex(), values));
                }
            }
        }
        else if (instance instanceof Entity entity) {
            var values = getRawIndex().getIndexDef().getValues(entity);
            var lastValue = values.get(values.size() - 1);
            if (lastValue.isArray()) {
                var array = lastValue.resolveArray();
                for (Value element : array.getElements()) {
                    var exceptLast = values.subList(0, values.size() - 1);
                    var fieldValues = new HashMap<IndexField, Value>();
                    var fieldIt = getRawIndex().getFields().iterator();
                    for (Value value : exceptLast) {
                        fieldValues.put(fieldIt.next(), value);
                    }
                    fieldValues.put(fieldIt.next(), element);
                    action.accept(new IndexKeyRT(getRawIndex(), fieldValues));
                }
            } else {
                var fieldValues = new HashMap<IndexField, Value>();
                var fieldIt = getRawIndex().getFields().iterator();
                for (Value value : values) {
                    fieldValues.put(fieldIt.next(), value);
                }
                action.accept(new IndexKeyRT(getRawIndex(), fieldValues));
            }
        }
        else
            throw new IllegalStateException("Cannot get index key for instance: " + instance);

    }


    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexRef(this);
    }

    @Override
    public ClassType getValueType() {
        return __klass__.getType();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        declaringType.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        declaringType.forEachReference(action);
        action.accept(indexReference);
    }
}
