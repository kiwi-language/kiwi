package org.metavm.meta.processor;

import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Flags;
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
                                 maker.Ident(names.fromString("__klass__")),
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
        trees.append(maker.VarDef(
                maker.Modifiers(Flags.PUBLIC | Flags.FINAL | Flags.STATIC),
                names.fromString("__klass__"),
                maker.Type(types.klass),
                maker.Apply(
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
                )
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
                   maker.Return(maker.Ident(names.fromString("__klass__")))
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
                                maker.Ident(names.fromString("__klass__")),
                                names.fromString("getType")
                        ), List.nil()))
                )),
                null
        ));
    }

}
