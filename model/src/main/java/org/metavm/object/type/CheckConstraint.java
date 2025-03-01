package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ConstraintDef;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityRegistry;
import org.metavm.expression.BinaryExpression;
import org.metavm.expression.InstanceEvaluationContext;
import org.metavm.expression.PropertyExpression;
import org.metavm.expression.UnaryExpression;
import org.metavm.flow.Value;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(32)
@Entity
public class CheckConstraint extends Constraint {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private Value condition;

    private transient ConstraintDef<?> constraintDef;

    public CheckConstraint(@NotNull Id id, Klass type, String name, String message, Value condition) {
        super(id, type, name, message);
        this.condition = condition;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        Constraint.visitBody(visitor);
        Value.visit(visitor);
    }

    @Override
    public String getDefaultMessage() {
        return "Constraint check failed";
    }

    public boolean isFieldConstraint(Field field) {
        if(condition.getExpression() instanceof BinaryExpression binExpr
                && binExpr.getLeft() instanceof PropertyExpression propExpr) {
            return propExpr.getProperty() == field;
        }
        else if(condition.getExpression() instanceof UnaryExpression unaryExpr
                && unaryExpr.getOperand() instanceof PropertyExpression propExr) {
            return propExr.getProperty() == field;
        }
        else {
            return false;
        }
    }

    @Override
    public String getDesc() {
        return "";
    }

    public Value getCondition() {
        return condition;
    }

    public boolean check(ClassInstance instance) {
        return Instances.isTrue(condition.evaluate(new InstanceEvaluationContext(instance)));
    }

    public ConstraintDef<?> getConstraintDef() {
        return constraintDef;
    }

    public void setConstraintDef(ConstraintDef<?> constraintDef) {
        this.constraintDef = constraintDef;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCheckConstraint(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        condition.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("defaultMessage", this.getDefaultMessage());
        map.put("desc", this.getDesc());
        map.put("condition", this.getCondition().toJson());
        map.put("constraintDef", this.getConstraintDef());
        var message = this.getMessage();
        if (message != null) map.put("message", message);
        map.put("name", this.getName());
        map.put("qualifiedName", this.getQualifiedName());
        map.put("declaringType", this.getDeclaringType().getStringId());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_CheckConstraint;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.condition = Value.read(input);
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        condition.write(output);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}
