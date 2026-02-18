package org.manul.meta.processor;

import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

class EntityTransformer {

    private final JCTree.JCClassDecl tree;
    private final TreeMaker maker;
    private final Names names;
    private final MyTypes types;


     EntityTransformer(JCTree.JCClassDecl tree, TreeMaker maker, Names names, MyTypes types) {
        this.tree = tree;
        this.maker = maker;
        this.names = names;
         this.types = types;
     }

    void transform() {
         var trees = new ListBuffer<JCTree>();
         addKlassField(trees);
         if (types.isAssignable(tree.sym.asType(), types.entity)) {
            addGetInstanceKlass(trees);
            addGetInstanceType(trees);
         }
         if (types.isAssignable(tree.sym.asType(), types.value))
             addGetValueType(trees);
         if (trees.nonEmpty())
             tree.defs = tree.defs.appendList(trees);
    }

    private void addGetValueType(ListBuffer<JCTree> trees) {
         trees.append(maker.MethodDef(
                 maker.Modifiers(Flags.PUBLIC),
                 names.fromString("getValueType"),
                 maker.Type(types.classType),
                 List.nil(),
                 List.nil(),
                 List.nil(),
                 maker.Block(0, List.of(
                         maker.Return(maker.Apply(null, maker.Select(
                                 maker.Apply(null, maker.Ident(names.fromString("__getKlass__")), List.nil()),
                                 names.fromString("getType")
                         ), List.nil()))
                 )),
                 null
         ));
    }

    private void addKlassField(ListBuffer<JCTree> trees) {
        for (Tree member : tree.getMembers()) {
            if (member instanceof VariableTree fieldTree && fieldTree.getName().contentEquals("__klass__"))
                return;
        }
        // Lazy-initialized to avoid circular static initialization between Type, Klass, and AnyType
        trees.append(maker.VarDef(
                maker.Modifiers(Flags.PUBLIC | Flags.STATIC),
                names.fromString("__klass__"),
                maker.Type(types.klass),
                null
        ));
        addGetKlassMethod(trees);
    }

    private void addGetKlassMethod(ListBuffer<JCTree> trees) {
        // Generates:
        // public static Klass __getKlass__() {
        //     if (__klass__ == null)
        //         __klass__ = StdKlassRegistry.instance.getKlass(ThisClass.class);
        //     return __klass__;
        // }
        var klassField = maker.Ident(names.fromString("__klass__"));
        var nullLit = maker.Literal(TypeTag.BOT, null);
        var condition = maker.Binary(JCTree.Tag.EQ, klassField, nullLit);
        var initExpr = maker.Apply(
                null,
                maker.Select(
                        maker.Select(
                                maker.Type(types.stdKlassRegistry),
                                names.fromString("instance")
                        ),
                        names.fromString("getKlass")
                ),
                List.of(
                        maker.Select(
                                maker.Ident(tree.getSimpleName()),
                                names._class
                        )
                )
        );
        var assignStmt = maker.Exec(maker.Assign(
                maker.Ident(names.fromString("__klass__")),
                initExpr
        ));
        var ifStmt = maker.If(condition, assignStmt, null);
        var returnStmt = maker.Return(maker.Ident(names.fromString("__klass__")));

        trees.append(maker.MethodDef(
                maker.Modifiers(Flags.PUBLIC | Flags.STATIC),
                names.fromString("__getKlass__"),
                maker.Type(types.klass),
                List.nil(),
                List.nil(),
                List.nil(),
                maker.Block(0, List.of(ifStmt, returnStmt)),
                null
        ));
    }

    private void addGetInstanceKlass(ListBuffer<JCTree> trees) {
        for (JCTree mem : tree.defs) {
            if (mem instanceof JCTree.JCMethodDecl meth && meth.getName().contentEquals("getInstanceKlass")
                    && meth.getParameters().isEmpty())
                return;
        }
        trees.append(maker.MethodDef(
                maker.Modifiers(Flags.PUBLIC),
                names.fromString("getInstanceKlass"),
                maker.Type(types.klass),
                List.nil(),
                List.nil(),
                List.nil(),
                maker.Block(0, List.of(
                   maker.Return(maker.Apply(null, maker.Ident(names.fromString("__getKlass__")), List.nil()))
                )),
                null
        ));
    }

    private void addGetInstanceType(ListBuffer<JCTree> trees) {
        for (JCTree mem : tree.defs) {
            if (mem instanceof JCTree.JCMethodDecl meth && meth.getName().contentEquals("getInstanceType")
                    && meth.getParameters().isEmpty())
                return;
        }
        trees.append(maker.MethodDef(
                maker.Modifiers(Flags.PUBLIC),
                names.fromString("getInstanceType"),
                maker.Type(types.classType),
                List.nil(),
                List.nil(),
                List.nil(),
                maker.Block(0, List.of(
                        maker.Return(maker.Apply(null, maker.Select(
                                maker.Apply(null, maker.Ident(names.fromString("__getKlass__")), List.nil()),
                                names.fromString("getType")
                        ), List.nil()))
                )),
                null
        ));
    }

}
