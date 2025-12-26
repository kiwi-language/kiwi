package org.metavm.wire.processor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.type.TypeMirror;

abstract class AbstractGenerator {

    final TreeMaker maker;
    final MyNames names;
    private int offset;
    private int nextVariableIndex;

    AbstractGenerator(TreeMaker maker, MyNames names, int beginPos) {
        this.maker = maker;
        this.names = names;
        maker.at(beginPos);
        offset = beginPos;
    }

    JCTree.JCFieldAccess makeSelect(JCTree.JCExpression selected, javax.lang.model.element.Name name) {
        next();
        return maker.Select(selected, (Name) name);
    }

    JCTree.JCFieldAccess makeSelect(JCTree.JCExpression selected, Symbol symbol) {
        next();
        return maker.Select(selected, symbol);
    }

    JCTree.JCIdent makeIdent(javax.lang.model.element.Name name) {
        next();
        return maker.Ident((Name) name);
    }

    JCTree.JCIdent makeThisIdent() {
        return makeIdent(names._this);
    }

    JCTree.JCTypeCast makeTypeCast(Type type, JCTree.JCExpression expr) {
        next();
        return maker.TypeCast(type, expr);
    }

    JCTree.JCLiteral makeNullLiteral() {
        next();
        return maker.Literal(TypeTag.BOT, null);
    }

    JCTree.JCLiteral makeLiteral(Object value) {
        next();
        return maker.Literal(value);
    }

    /** @noinspection SameParameterValue*/
    JCTree.JCMethodDecl makeMethod(long mods, Name name, JCTree.JCExpression retType, List<JCTree.JCVariableDecl> params, List<JCTree.JCStatement> stats) {
        next();
        return maker.MethodDef(
                maker.Modifiers(mods),
                name,
                retType,
                List.nil(),
                params,
                List.nil(),
                maker.Block(0, stats),
                null
        );
    }

    JCTree.JCBlock makeBlock(List<JCTree.JCStatement> stats) {
        return maker.Block(0, stats);
    }

    JCTree.JCVariableDecl makeParam(javax.lang.model.element.Name name, TypeMirror type) {
        return makeParam(name, makeType(type));
    }

    JCTree.JCVariableDecl makeParam(javax.lang.model.element.Name name, JCTree.JCExpression type) {
        next();
        return maker.VarDef(
                maker.Modifiers(Flags.PARAMETER),
                (Name) name,
                type,
                null
        );
    }

    JCTree.JCExpression makeType(TypeMirror type) {
        next();
        return maker.Type((Type) type);
    }

    JCTree.JCNewClass makeNew(Type.ClassType clazz, List<JCTree.JCExpression> args) {
        next();
        return maker.NewClass(null, List.nil(), makeType(clazz), args, null);
    }

    JCTree.JCMethodInvocation makeApply(JCTree.JCExpression fn, JCTree.JCExpression...args) {
        if (args.length == 0)
            return makeApply(fn, List.nil());
        else {
            var listBuf = new ListBuffer<JCTree.JCExpression>();
            for (JCTree.JCExpression arg : args) {
                listBuf.append(arg);
            }
            return makeApply(fn, listBuf.toList());
        }
    }

    JCTree.JCMethodInvocation makeApply(JCTree.JCExpression fn, List<JCTree.JCExpression> args) {
        next();
        return maker.Apply(null, fn, args);
    }

    boolean isAnnotationPresent(AnnotatedConstruct construct, Symbol.ClassSymbol clazz) {
        return Annotations.isAnnotationPresent(construct, clazz);
    }

    void next() {
        maker.at(offset++);
    }

    Name nextVarName() {
        return names.fromString("var" + nextVariableIndex++);
    }

}
