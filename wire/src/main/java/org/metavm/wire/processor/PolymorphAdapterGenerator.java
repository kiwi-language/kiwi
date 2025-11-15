package org.metavm.wire.processor;

import com.sun.source.tree.CaseTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

public class PolymorphAdapterGenerator extends AbstractAdapterGenerator {

    private final MyMethods methods;

    PolymorphAdapterGenerator(Clazz clazz, TreeMaker treeMaker, MyNames names, MyTypes types, MyClasses myClasses, MyMethods methods) {
        super(clazz, treeMaker, names, types, myClasses);
        this.methods = methods;
    }

    @Override
    protected List<JCTree.JCStatement> generateWriteBody() {
        var cases = new ListBuffer<JCTree.JCCase>();
        var hasDefault = false;
        for (SubType subType : clazz.subTypes()) {
            JCTree.JCCaseLabel caseLabel;
            JCTree.JCExpression expr;
            if (subType.symbol() == clazz.symbol()) {
                hasDefault = true;
                caseLabel = maker.DefaultCaseLabel();
                var args = new ListBuffer<JCTree.JCExpression>();
                args.append(makeIdent(names.output));
                if (clazz.parentField() != null)
                    args.append(makeIdent(names.parent));
                clazz.forEachFieldAdapter(adapter -> args.append(makeIdent(adapter.name())));
                expr = makeApply(
                        makeSelect(makeIdent(names.o), names.__write__),
                        args.toList()
                );
            } else {
                var var = nextVarName();
                caseLabel = maker.PatternCaseLabel(maker.BindingPattern(
                        maker.VarDef(
                                maker.Modifiers(0),
                                var,
                                makeType(subType.symbol().type),
                                null
                        )
                ));
                var adapterName = clazz.getAdapter(subType.symbol()).name();
                expr = makeApply(
                        makeSelect(makeIdent(names.output), methods.writeEntity),
                        makeIdent(var),
                        makeIdent(adapterName)
                );
            }
            cases.append(maker.Case(
                    CaseTree.CaseKind.RULE,
                    List.of(caseLabel),
                    null,
                    List.of(
                            maker.Exec(makeApply(
                                    makeSelect(makeIdent(names.output), methods.writeInt),
                                    makeLiteral(subType.tag())
                            )),
                            maker.Exec(expr)
                    ),
                    null
            ));
        }
        if (!hasDefault) {
            cases.append(maker.Case(
                    CaseTree.CaseKind.RULE,
                    List.of(maker.DefaultCaseLabel()),
                    null,
                    List.of(maker.Throw(makeNew(
                            (Type.ClassType) myClasses.wireIOException.type,
                            List.of(makeLiteral("Unknown entity type"))
                    ))),
                    null
            ));
        }
        return List.of(maker.Switch(
                makeIdent(names.o),
                cases.toList()
        ));
    }

    @Override
    protected List<JCTree.JCStatement> generateReadBody() {
        var cases = new ListBuffer<JCTree.JCCase>();
        for (SubType subType : clazz.subTypes()) {
            JCTree.JCExpression expr;
            if (subType.symbol() == clazz.symbol()) {
                var args = new ListBuffer<JCTree.JCExpression>();
                args.append(makeIdent(names.input));
                if (clazz.parentField() != null)
                    args.append(makeIdent(names.parent));
                clazz.forEachFieldAdapter(adapter -> args.append(makeIdent(adapter.name())));
                expr = makeNew(clazz.type(), args.toList());
            } else {
                var adapterName = clazz.getAdapter(subType.symbol()).name();
                expr = makeApply(
                        makeSelect(makeIdent(names.input), methods.readEntity),
                        makeIdent(adapterName), makeIdent(names.parent)
                );
            }
            cases.append(maker.Case(
                    CaseTree.CaseKind.RULE,
                    List.of(maker.ConstantCaseLabel(makeLiteral(subType.tag()))),
                    null,
                    List.of(maker.Yield(expr)),
                    null
            ));
        }
        cases.append(maker.Case(
                CaseTree.CaseKind.RULE,
                List.of(maker.DefaultCaseLabel()),
                null,
                List.of(maker.Throw(makeNew((Type.ClassType) myClasses.wireIOException.type, List.of(
                        maker.Literal("Unknown type")
                )))),
                null
        ));
        return List.of(
                maker.Return(maker.SwitchExpression(
                        makeApply(makeSelect(makeIdent(names.input), methods.readInt)),
                        cases.toList()
                ))
        );
    }

    @Override
    protected List<JCTree.JCStatement> generateVisitBody() {
        var key = makeApply(makeSelect(makeIdent(names.visitor), methods.visitInt));
        var cases = new ListBuffer<JCTree.JCCase>();
        for (SubType subType : clazz.subTypes()) {
            JCTree.JCExpression expr;
            if (subType.symbol() == clazz.symbol()) {
                var args = new ListBuffer<JCTree.JCExpression>();
                args.append(makeIdent(names.visitor));
                clazz.forEachFieldAdapter(adapter -> args.append(makeIdent(adapter.name())));
                expr = makeApply(
                        makeSelect(makeType(clazz.type()), names.__visit__),
                        args.toList()
                );
            } else {
                var adapterName = clazz.getAdapter(subType.symbol()).name();
                expr = makeApply(
                        makeSelect(makeIdent(names.visitor), methods.visitEntity),
                        makeIdent(adapterName)
                );
            }
            cases.append(maker.Case(
                    CaseTree.CaseKind.RULE,
                    List.of(maker.ConstantCaseLabel(makeLiteral(subType.tag()))),
                    null,
                    List.of(maker.Exec(expr)),
                    null
            ));
        }
        cases.append(maker.Case(
                CaseTree.CaseKind.RULE,
                List.of(maker.DefaultCaseLabel()),
                null,
                List.of(maker.Throw(makeNew((Type.ClassType) myClasses.wireIOException.type, List.of(
                        maker.Literal("Unknown type")
                )))),
                null
        ));
        return List.of(maker.Switch(key, cases.toList()));
    }
}