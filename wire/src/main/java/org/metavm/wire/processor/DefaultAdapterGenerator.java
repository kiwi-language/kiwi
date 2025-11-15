package org.metavm.wire.processor;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

class DefaultAdapterGenerator extends AbstractAdapterGenerator {

    DefaultAdapterGenerator(Clazz clazz, TreeMaker treeMaker, MyNames names, MyTypes types, MyClasses myClasses) {
        super(clazz, treeMaker, names, types, myClasses);
    }

    @Override
    protected List<JCTree.JCStatement> generateWriteBody() {
        var args = new ListBuffer<JCTree.JCExpression>();
        args.append(makeIdent(names.output));
        clazz.forEachFieldAdapter(adapter -> args.append(makeIdent(adapter.name())));
        return List.of(maker.Exec(makeApply(
                makeSelect(makeIdent(names.o), names.__write__),
                args.toList()
        )));
    }

    @Override
    protected List<JCTree.JCStatement> generateReadBody() {
        if (clazz.symbol().isAbstract()) {
            return List.of(maker.Throw(makeNew(
                    (Type.ClassType) myClasses.unsupportedOperationException.type, List.nil()
            )));
        }
        else {
            var args = new ListBuffer<JCTree.JCExpression>();
            args.append(makeIdent(names.input));
            var parentField = clazz.getParentField();
            if (parentField != null)
                args.append(makeTypeCast(parentField.type(), makeIdent(names.parent)));
            clazz.forEachFieldAdapter(adapter -> args.append(makeIdent(adapter.name())));
            var newExpr = maker.NewClass(null, List.nil(),
                    makeType(clazz.type()),
                    args.toList(),
                    null
            );
            return List.of(maker.Return(newExpr));
        }
    }

    @Override
    protected List<JCTree.JCStatement> generateVisitBody() {
        var args = new ListBuffer<JCTree.JCExpression>();
        args.append(makeIdent(names.visitor));
        clazz.forEachFieldAdapter(adapter -> args.append(makeIdent(adapter.name())));
        return List.of(maker.Exec(makeApply(
                makeSelect(makeType(clazz.type()), names.__visit__),
                args.toList()
        )));
    }

}
