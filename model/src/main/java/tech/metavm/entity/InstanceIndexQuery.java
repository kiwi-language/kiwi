package tech.metavm.entity;

import tech.metavm.expression.InstanceEvaluationContext;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Index;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

public record InstanceIndexQuery(
        Index index,
        List<InstanceIndexQueryItem> items,
        boolean desc,
        Long limit) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (InstanceIndexQuery) obj;
        return Objects.equals(this.index, that.index) &&
                Objects.equals(this.items, that.items) &&
                this.desc == that.desc &&
                Objects.equals(this.limit, that.limit);
    }

    public boolean matches(ClassInstance instance, IEntityContext context) {
        var evaluationContext = new InstanceEvaluationContext(instance, context);
        return NncUtils.allMatch(items, item -> item.matches(evaluationContext));
    }

    @Override
    public String toString() {
        return "InstanceIndexQuery[" +
                "index=" + index + ", " +
                "items=" + items + ", " +
                "desc=" + desc + ", " +
                "limit=" + limit + ']';
    }


}
