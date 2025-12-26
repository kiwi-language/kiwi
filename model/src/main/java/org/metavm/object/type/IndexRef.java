package org.metavm.object.type;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.metavm.common.ErrorCode;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.Entity;
import org.metavm.entity.Writable;
import org.metavm.flow.Flows;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@Slf4j
@org.metavm.api.Entity
public class IndexRef implements org.metavm.entity.Reference, Writable {

    @Getter
    private final ClassType declaringType;
    private final Reference indexReference;

    public IndexRef(ClassType declaringType, Index rawIndex) {
        this(declaringType, rawIndex.getReference());
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

    public List<IndexKeyRT> createIndexKey(ClassInstance instance) {
        var result = new ArrayList<IndexKeyRT>();
        forEachIndexKey(instance, result::add);
        return result;
    }

    public void forEachIndexKey(Instance instance, Consumer<IndexKeyRT> action) {
        if (instance instanceof MvClassInstance clsInst) {
            var method = getRawIndex().getMethod();
            if (method != null) {
                try {
                    var entityContext = ContextUtil.getEntityContext();
                    var values = method.isStatic() ?
                            Indexes.getIndexValues(this, requireNonNull(Flows.invoke(method.getRef(), null, List.of(clsInst.getReference()), entityContext))) :
                            Indexes.getIndexValues(this, requireNonNull(Flows.invoke(declaringType.getMethod(method), clsInst, List.of(), entityContext)));
                    action.accept(new IndexKeyRT(getRawIndex(), values));
                } catch (BusinessException e) {
                    throw new BusinessException(ErrorCode.INDEX_KEY_COMPUTE_ERROR, getRawIndex().getQualifiedName(), e.getMessage());
                }
            } else {
                throw new IllegalStateException("Index creator is missing");
            }
        }
        else if (instance instanceof Entity entity) {
            var values = getRawIndex().getIndexDef().getValues(entity);
            if (values.getLast() instanceof ValueReference ref && ref.get() instanceof ArrayInstance array) {
                for (Value element : array.getElements()) {
                    var exceptLast = values.subList(0, values.size() - 1);
                    var fieldValues = new ArrayList<>(exceptLast);
                    fieldValues.add(element);
                    action.accept(new IndexKeyRT(getRawIndex(), fieldValues));
                }
            } else {
                var fieldValues = new ArrayList<>(values);
                action.accept(new IndexKeyRT(getRawIndex(), fieldValues));
            }
        }
        else
            throw new IllegalStateException("Cannot get index key for instance: " + instance);
    }

    public Type getType() {
        return getRawIndex().getType(declaringType.getTypeMetadata());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexRef(this);
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
