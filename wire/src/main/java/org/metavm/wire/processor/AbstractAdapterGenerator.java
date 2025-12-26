package org.metavm.wire.processor;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import java.util.Objects;
import java.util.function.Consumer;

abstract class AbstractAdapterGenerator extends AbstractGenerator {

    protected final MyTypes types;
    protected final MyClasses myClasses;
    protected final Clazz clazz;

    AbstractAdapterGenerator(Clazz clazz, TreeMaker treeMaker, MyNames names, MyTypes types, MyClasses myClasses) {
        super(treeMaker, names, clazz.pos());
        this.clazz = clazz;
        this.types = types;
        this.myClasses = myClasses;
    }

    public JCTree.JCClassDecl generate() {
        var defs = new ListBuffer<JCTree>();
        generateFields(defs::add);
        defs.append(generateInit());
        defs.append(generateWrite());
        defs.append(generateRead());
        defs.append(generateVisit());
        defs.append(generateGetSupportedTypes());
        defs.append(generateGetTag());
        return maker.ClassDef(
                maker.Modifiers(Flags.PUBLIC | Flags.STATIC),
                names.__WireAdapter__,
                List.nil(),
                null,
                List.of(
                        maker.TypeApply(
                                makeType(types.wireAdapter),
                                List.of(makeType(clazz.symbol().asType()))
                        )
                ),
                defs.toList()
        );
    }

    private void generateFields(Consumer<? super JCTree.JCVariableDecl> addField) {
        clazz.forEachAdapter(adapter -> {
            addField.accept(maker.VarDef(
                    maker.Modifiers(Flags.PRIVATE),
                    adapter.name(),
                    maker.Type(adapter.type()),
                    null
            ));
        });
    }

    private JCTree.JCMethodDecl generateInit() {
        var stmts = new ListBuffer<JCTree.JCStatement>();
        clazz.forEachAdapter(adapter -> {
            var valueClass = (Symbol.ClassSymbol) adapter.valueType().tsym;
            stmts.append(maker.Exec(maker.Assign(
                    maker.Ident(adapter.name()),
                    maker.Apply(
                            List.nil(),
                            maker.Select(
                                    maker.Ident(names.adapterRegistry),
                                    names.getAdapter
                            ),
                            List.of(maker.ClassLiteral(valueClass))
                    )
            )));
        });
        return makeMethod(
                Flags.PUBLIC,
                names.init,
                maker.TypeIdent(TypeTag.VOID),
                List.of(
                        makeParam(
                                names.adapterRegistry,
                                makeType(types.adapterRegistry)
                        )
                ),
                stmts.toList()
        );
    }

    private JCTree.JCMethodDecl generateWrite() {
        return makeMethod(
                Flags.PUBLIC,
                names.write,
                maker.TypeIdent(TypeTag.VOID),
                List.of(
                        makeParam(names.o, makeType(clazz.type())),
                        makeParam(names.output, makeType(types.wireOutput))
                ),
                generateWriteBody()
        );
    }

    protected abstract List<JCTree.JCStatement> generateWriteBody();

    private JCTree.JCMethodDecl generateRead() {
        return makeMethod(
                Flags.PUBLIC,
                names.read,
                makeType(clazz.type()),
                List.of(
                        makeParam(names.input, makeType(types.wireInput)),
                        makeParam(names.parent, makeType(types.object))
                ),
                generateReadBody()
        );
    }

    protected abstract List<JCTree.JCStatement> generateReadBody();

    private JCTree.JCMethodDecl generateVisit() {
        return makeMethod(
                Flags.PUBLIC,
                names.visit,
                maker.TypeIdent(TypeTag.VOID),
                List.of(makeParam(names.visitor, makeType(types.wireVisitor))),
                generateVisitBody()
        );
    }

    protected abstract List<JCTree.JCStatement> generateVisitBody();

    private JCTree.JCMethodDecl generateGetSupportedTypes() {
        return makeMethod(
                Flags.PUBLIC,
                names.getSupportedTypes,
                maker.TypeApply(
                        makeType(types.list),
                        List.of(maker.TypeApply(
                                makeType(types.clazz),
                                List.of(
                                        maker.Wildcard(
                                                maker.TypeBoundKind(BoundKind.EXTENDS),
                                                makeType(clazz.type())
                                        )
                                )
                        ))
                ),
                List.nil(),
                List.of(
                        maker.Return(
                                makeApply(
                                        makeSelect(maker.Type(types.list), names.of),
                                        maker.Select(
                                                makeType(clazz.type()),
                                                names._class
                                        )
                                )
                        )
                )
        );
    }

    private JCTree.JCMethodDecl generateGetTag() {
        var value = Annotations.getAttribute(clazz.symbol(), myClasses.wire, names.value);
        var tag = (int) Objects.requireNonNullElse(value, -1);
        return makeMethod(
                Flags.PUBLIC,
                names.getTag,
                maker.TypeIdent(TypeTag.INT),
                List.nil(),
                List.of(maker.Return(maker.Literal(tag)))
        );
    }

}
