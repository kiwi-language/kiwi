package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.ValueObject;
import org.metavm.entity.Element;
import org.metavm.entity.EntityParentRef;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

@Entity
public abstract class Value extends Element implements ValueObject {

    public Value() {
        this(null);
    }

    public Value(@Nullable EntityParentRef parentRef) {
        super(null, parentRef);
    }

    public abstract Type getType();

    @NotNull
    public abstract org.metavm.object.instance.core.Value evaluate(EvaluationContext context);

    public abstract String getText();

    public abstract Expression getExpression();

}
