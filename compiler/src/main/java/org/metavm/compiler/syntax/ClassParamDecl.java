package org.metavm.compiler.syntax;

import org.jetbrains.annotations.Nullable;
import org.metavm.compiler.element.Field;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.element.Param;
import org.metavm.compiler.util.List;

import java.util.Objects;

public class ClassParamDecl extends VariableDecl<Param> {

    private List<Modifier> mods;
    private List<Annotation> annotations;
    private boolean withField;
    private Field field;

    public ClassParamDecl(List<Modifier> mods, List<Annotation> annotations, boolean withField, boolean mutable, @Nullable TypeNode type, Name name) {
        super(List.nil(), type, name, null, mutable);
        this.mods = mods;
        this.annotations = annotations;
        this.withField = withField;
    }

    @Override
    public void write(SyntaxWriter writer) {
        if (mods.nonEmpty()) {
            writer.write(mods);
            writer.write(" ");
        }
        if (withField)
            writer.write(isMutable() ? "var " : "val ");
        writer.write(getName());
        if (getType() != null) {
            writer.write(": ");
            writer.write(getType());
        }
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitClassParamDecl(this);
    }

    public List<Modifier> getMods() {
        return mods;
    }

    public void setMods(List<Modifier> mods) {
        this.mods = mods;
    }

    @Override
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public boolean isWithField() {
        return withField;
    }

    public void setWithField(boolean withField) {
        this.withField = withField;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    @Override
    public ClassParamDecl setPos(int pos) {
        return (ClassParamDecl) super.setPos(pos);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ClassParamDecl that = (ClassParamDecl) object;
        return isMutable() == that.isMutable() && withField == that.withField && Objects.equals(mods, that.mods) && Objects.equals(annotations, that.annotations) && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mods, annotations, isMutable(), withField, field);
    }
}
