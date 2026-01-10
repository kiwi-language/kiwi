package org.manul.compiler.type;

import org.jetbrains.annotations.Nullable;
import org.manul.compiler.element.ElementTable;
import org.manul.compiler.element.ElementWriter;
import org.manul.compiler.element.Func;
import org.manul.compiler.syntax.ErrorTypeNode;
import org.manul.compiler.syntax.TypeNode;
import org.manul.compiler.generate.KlassOutput;

public class ErrorType implements Type {

    public static final ErrorType instance = new ErrorType();

    private ErrorType() {
    }

    @Override
    public void write(KlassOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeType(ElementWriter writer) {
        writer.write("<error>");
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        return true;
    }

    @Override
    public <R> R accept(TypeVisitor<R> visitor) {
        return visitor.visitErrorType(this);
    }

    @Override
    public int getTag() {
        return TypeTags.TAG_ERROR;
    }

    @Override
    public ElementTable getTable() {
        return new ElementTable();
    }

    @Override
    public String getInternalName(@Nullable Func current) {
        return "<error>";
    }

    @Override
    public TypeNode makeNode() {
        return new ErrorTypeNode();
    }

    @Override
    public Closure getClosure() {
        return Closure.nil;
    }
}
