package org.metavm.object.type;

import org.metavm.entity.*;
import org.metavm.entity.Reference;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.InstanceEvaluationContext;
import org.metavm.flow.Flows;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.ElementValue;
import org.metavm.object.instance.core.Value;
import org.metavm.util.*;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class IndexRef extends ElementValue implements Reference, Writable {

    private final ClassType declaringType;
    private final Index rawIndex;

    public IndexRef(ClassType declaringType, Index rawIndex) {
        this.declaringType = declaringType;
        this.rawIndex = rawIndex;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexRef(this);
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof IndexRef that && that.declaringType.equals(declaringType) && that.rawIndex == rawIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringType, rawIndex);
    }

    @Override
    public Type getType() {
        return StdKlass.indexRef.type();
    }

    public void write(MvOutput output) {
        output.write(WireTypes.INDEX_REF);
        declaringType.write(output);
        output.writeEntityId(rawIndex);
    }

    public static IndexRef read(MvInput input) {
        var classType = (ClassType) input.readType();
        var rawIndex = input.getIndex(input.readId());
        return new IndexRef(classType, rawIndex);
    }

    public Index getRawIndex() {
        return rawIndex;
    }

    public String getName() {
        return rawIndex.getName();
    }

    public int getFieldCount() {
        return rawIndex.getNumFields();
    }

    public List<Type> getIndexFieldTypes() {
        return NncUtils.map(rawIndex.getFields(), f -> f.getType(declaringType.getTypeMetadata()));
    }

    public ClassType getDeclaringType() {
        return declaringType;
    }

    public List<IndexKeyRT> createIndexKey(ClassInstance instance) {
        var result = new ArrayList<IndexKeyRT>();
        forEachIndexKey(instance, result::add);
        return result;
    }

    public void forEachIndexKey(ClassInstance instance, Consumer<IndexKeyRT> action) {
        EvaluationContext evaluationContext = new InstanceEvaluationContext(instance);
        Map<IndexField, Value> values = new HashMap<>();
        var method = rawIndex.getMethod();
        var fields = rawIndex.getFields();
        if(method != null) {
            var entityContext = ContextUtil.getEntityContext();
            var indexValues =
                    method.isStatic() ?
                            Indexes.getIndexValues(this, requireNonNull(Flows.execute(method.getRef(), null, List.of(instance.getReference()),entityContext).ret())) :
                            Indexes.getIndexValues(this, requireNonNull(Flows.execute(declaringType.getMethod(method), instance, List.of(), entityContext).ret()));
            var lastIdx = fields.size() - 1;
            for (int i = 0; i < lastIdx; i++) {
                var field = fields.get(i);
                values.put(field, indexValues.get(i));
            }
            // When the last index item is an array, create an index key for each element.
            var lastField = fields.get(lastIdx);
            if (lastField.getValue().getType().getUnderlyingType().isArray()) {
                var lastValues = new HashSet<>(indexValues.get(lastIdx).resolveArray().getElements());
                for (Value lastValue : lastValues) {
                    values.put(lastField, lastValue);
                    action.accept(new IndexKeyRT(rawIndex, values));
                }
            } else {
                values.put(lastField, indexValues.get(lastIdx));
                action.accept(new IndexKeyRT(rawIndex, values));
            }
        }
        else {
            for (int i = 0; i < fields.size() - 1; i++) {
                var field = fields.get(i);
                values.put(field, field.getValue().evaluate(evaluationContext));
            }
            // When the last index item is an array, create an index key for each element.
            var lastField = fields.get(fields.size() - 1);
            if (lastField.getValue().getType().getUnderlyingType().isArray()) {
                var lastValues = new HashSet<>((lastField.getValue().evaluate(evaluationContext)).resolveArray().getElements());
                List<IndexKeyRT> keys = new ArrayList<>();
                for (Value lastValue : lastValues) {
                    values.put(lastField, lastValue);
                    action.accept(new IndexKeyRT(rawIndex, values));
                }
            } else {
                values.put(lastField, lastField.getValue().evaluate(evaluationContext));
                action.accept(new IndexKeyRT(rawIndex, values));
            }
        }
    }



}
