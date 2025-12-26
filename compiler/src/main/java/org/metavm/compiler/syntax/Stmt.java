package org.metavm.compiler.syntax;

public abstract class Stmt extends Node {

    @Override
    public Stmt setPos(int pos) {
        return (Stmt) super.setPos(pos);
    }
}
