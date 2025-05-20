package org.metavm.compiler.syntax;

import org.jetbrains.annotations.Nullable;
import org.metavm.compiler.element.Field;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.element.Param;
import org.metavm.compiler.util.List;

public class ClassParamDecl extends VariableDecl<Param> {

    private List<Modifier> mods;
    private List<Annotation> annotations;
    private boolean readonly;
    private boolean withField;
    private Field field;

    public ClassParamDecl(List<Modifier> mods, List<Annotation> annotations, boolean withField, boolean readonly, @Nullable TypeNode type, Name name) {
        super(List.nil(), type, name, null);
        this.mods = mods;
        this.annotations = annotations;
        this.readonly = readonly;
        this.withField = withField;
    }

    @Override
    public void write(SyntaxWriter writer) {
        if (mods.nonEmpty()) {
            writer.write(mods);
            writer.write(" ");
        }
        if (withField)
            writer.write(readonly ? "val " : "var ");
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

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}
