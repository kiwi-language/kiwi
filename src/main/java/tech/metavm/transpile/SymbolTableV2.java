package tech.metavm.transpile;

import tech.metavm.transpile.ir.*;
import tech.metavm.util.InternalException;

import java.util.*;

class SymbolTableV2 {

    private final RootBlock rootBlock = new RootBlock();
    private final LinkedList<IBlock> blockStack = new LinkedList<>();

    SymbolTableV2() {
        blockStack.push(rootBlock);
    }

    void addPackage(IRPackage pkg) {
        rootBlock.addPackage(pkg);
    }

    void addClass(IRClass klass) {
        rootBlock.addClass(klass);
    }

    IBlock currentBlock() {
        if(isInBlock()) {
            return blockStack.peek();
        }
        else {
            throw new InternalException("Currently not in any block");
        }
    }

    boolean isInBlock() {
        return !blockStack.isEmpty();
    }

    IRMethod currentMethod() {
        var block = currentBlock();
        while (true) {
            if(block instanceof MethodBlock methodBlock) {
                return methodBlock.method();
            }
            if(block instanceof ClassBlock || block.parent() == null) {
                break;
            }
            block = block.parent();
        }
        throw new InternalException("Current not in a method");
    }

    IRClass currentClass() {
        var block = currentBlock();
        while (block != null) {
            if(block instanceof ClassBlock classBlock) {
                return classBlock.klass();
            }
            block = block.parent();
        }
        throw new InternalException("Currently not inside any class");
    }


    CodeBlock currentCodeBlock() {
        if(blockStack.peek() instanceof CodeBlock codeBlock) {
            return codeBlock;
        }
        throw new InternalException("Currently not in a code block");
    }

    IValueSymbol resolveValue(String name) {
        return currentBlock().resolveValue(name);
    }

    IValueSymbol tryResolveValue(String name) {
        return currentBlock().tryResolveValue(name);
    }

    IRClass tryResolveClass(String name) {
        return currentBlock().tryResolveClass(name);
    }

    ISymbol resolveValueOrClass(String name) {
        var block = currentBlock();
        var v = block.tryResolveValue(name);
        return v != null ? v : block.resolveClass(name);
    }

    void enterBlock(IBlock block) {
        blockStack.push(block);
    }

    void exitBlock() {
        blockStack.pop();
    }

    String getQualifiedName(String name) {
        return rootBlock.getQualifiedName(name);
    }

    public void importStaticMember(IRClass klass, String name) {
        if(klass.isClassDeclared(name)) {
            rootBlock.addClass(klass.getClass(name));
        }
        if(klass.isFieldDeclared(name)) {
            rootBlock.addStaticField(klass.getField(name));
        }
        if(klass.isMethodDeclared(name)) {
            klass.getMethods(name).forEach(rootBlock::addStaticMethod);
        }
    }

    public void importAllStaticMembers(IRClass klass) {
        rootBlock.addAllStaticMembers(klass);
    }

    public void importClass(IRClass klass) {
        rootBlock.addClass(klass);
    }

    public IRMethod resolveMethod(String name, List<IRType> parameterType) {
        return currentBlock().resolveMethod(name, parameterType);
    }
}
