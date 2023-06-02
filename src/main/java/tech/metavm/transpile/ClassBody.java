package tech.metavm.transpile;

import tech.metavm.transpile.ir.CodeBlock;
import tech.metavm.transpile.ir.IRClass;
import tech.metavm.transpile.ir.IRConstructor;
import tech.metavm.transpile.ir.IRField;
import tech.metavm.transpile.ir.IRMethod;

import java.util.ArrayList;
import java.util.List;

public record ClassBody(
        List<IRConstructor> constructors,
        List<IRField> fields,
        List<IRMethod> methods,
        List<IRClass> classes,
        List<CodeBlock> staticBlocks
) {

    public static ClassBody create() {
        return new ClassBody(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public void addField(IRField field) {
        this.fields.add(field);
    }

    public void addMethod(IRMethod method) {
        this.methods.add(method);
    }

    public void addClass(IRClass klass) {
        this.classes.add(klass);
    }

    public void addStaticBlock(CodeBlock block) {
        staticBlocks.add(block);
    }

    public void addConstructor(IRConstructor constructor) {
        this.constructors.add(constructor);
    }

}
