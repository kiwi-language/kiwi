package org.metavm.wire.processor;

import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import org.metavm.wire.util.Pair;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

class WireTransformer extends AbstractGenerator {

    private final Clazz clazz;
    private final Trees trees;
    private final MyNames names;
    private final MyTypes types;
    private final MyClasses classes;
    private final MyMethods methods;
    private final Introspects introspects;
    private final TreeCopier<Void> copier;
    private int nextAdapterIndex;
    private final Map<Symbol.TypeSymbol, Name> adapterNames = new LinkedHashMap<>();

    WireTransformer(Clazz clazz, TreeMaker maker, Trees trees, MyNames names, MyTypes types, MyClasses classes, MyMethods methods, Introspects introspects) {
        super(maker, names, clazz.pos());
        this.clazz = clazz;
        this.trees = trees;
        this.names = names;
        this.types = types;
        this.classes = classes;
        this.methods = methods;
        this.introspects = introspects;
        copier = new TreeCopier<>(maker);
    }

    /**
     * Generate a constructor for creating instances from the input stream
     */
    public void transform() {
        if (!clazz.symbol().isRecord())
            clearDefaultConstrFlag();
        calcAdapterNames();
        lowerFieldInits();
        var newMembers = new ListBuffer<JCTree>();
        if (!constrExists())
            newMembers.append(generateConstr());
        if (!writeExists())
            newMembers.append(generateWrite());
        if(!visitExists())
            newMembers.append(generateVisit());
        var tree = (JCTree.JCClassDecl) trees.getTree(clazz.symbol());
        tree.defs = tree.defs.appendList(newMembers);
    }

    private void calcAdapterNames() {
        clazz.forEachAdapter(adapter -> {
            var paramName = nextAdapterName();
            adapterNames.put(adapter.valueType().tsym, paramName);
        });
    }

    private boolean constrExists() {
        var paramTypes = new ListBuffer<Type>();
        paramTypes.append(types.wireInput);
        var parentField = clazz.getParentField();
        if (parentField != null)
            paramTypes.append(parentField.type());
        clazz.forEachFieldAdapter(adapter -> {
            paramTypes.append(adapter.type());
        });
        return methods.methodExists(clazz.symbol(), names._init, paramTypes.toList());
    }

    private JCTree.JCMethodDecl generateConstr() {
        var stats = new ListBuffer<JCTree.JCStatement>();
        var params = new ListBuffer<JCTree.JCVariableDecl>();
        params.append(makeParam(names.input, types.wireInput));
        var parentField = clazz.getParentField();
        if (parentField != null)
            params.append(makeParam(names.parent, parentField.type()));
        clazz.forEachFieldAdapter(adapter -> {
            var paramName = adapterNames.get(adapter.valueType().tsym);
            params.append(makeParam(paramName, adapter.type()));
        });
        if (clazz.superClass() != null) {
            var superCl = clazz.superClass();
            var superCallArgs = new ListBuffer<JCTree.JCExpression>();
            superCallArgs.append(makeIdent(names.input));
            if (superCl.getParentField() != null)
                superCallArgs.append(makeIdent(names.parent));
            superCl.forEachFieldAdapter(adapter -> {
                var paramName = adapterNames.get(adapter.valueType().tsym);
                superCallArgs.append(makeIdent(paramName));
            });
            stats.append(maker.Exec(maker.Apply(
                    List.nil(),
                    makeIdent(names._super),
                    superCallArgs.toList()
            )));
        }
        generateConstrBody(stats);
        return makeMethod(
                Flags.PROTECTED,
                names._init,
                null,
                params.toList(),
                stats.toList()
        );

    }

    private void lowerFieldInits() {
        var tree = (JCTree.JCClassDecl) trees.getTree(clazz.symbol());
        var constrs = new ListBuffer<JCTree.JCMethodDecl>();
        java.util.List<Pair<JCTree.JCVariableDecl, JCTree.JCExpression>> inits = new ArrayList<>();
        for (JCTree mem : tree.defs) {
            if (mem instanceof JCTree.JCVariableDecl field
                    && field.getInitializer() != null
                    && (field.getModifiers().flags & Flags.STATIC) == 0) {
                inits.add(new Pair<>(field, field.getInitializer()));
                field.init = null;
            }
            if (mem instanceof JCTree.JCMethodDecl method && method.name == names._init) {
                constrs.append(method);
            }
        }
        for (JCTree.JCMethodDecl constr : constrs) {
            var stats = constr.body.stats;
            JCTree.JCStatement head = null;
            var tail = stats;
            if (stats.nonEmpty()) {
                var first = stats.head;
                if (first instanceof JCTree.JCExpressionStatement exprStmt && exprStmt.expr instanceof JCTree.JCMethodInvocation invoke) {
                    var fn = invoke.meth;
                    if(fn instanceof JCTree.JCIdent ident) {
                        if (ident.name == names._this)
                            continue;
                        if (ident.name == names._super) {
                            head = first;
                            tail = stats.tail;
                        }
                    }
                }
            }
            inits = inits.reversed();
            var newStats = tail;
            for (var init : inits) {
                var stat = maker.Exec(maker.Assign(
                        makeSelect(makeThisIdent(), init.first().name),
                        copier.copy(init.second())
                ));
                newStats = newStats.prepend(stat);
            }
            if (head != null)
                newStats = newStats.prepend(head);
            constr.body.stats = newStats;
        }
    }

    private void generateConstrBody(ListBuffer<JCTree.JCStatement> stats) {
        if (clazz.symbol().isRecord()) {
            var args = new ListBuffer<JCTree.JCExpression>();
            for (Field field : clazz.instanceFields()) {
                if (field.parent())
                    args.append(makeIdent(names.parent));
                else
                    args.append(generateRead(field.type(), field.nullable()));
            }
            stats.append(maker.Exec(maker.Apply(
                    List.nil(),
                    makeThisIdent(),
                    args.toList()))
            );
        } else {
            for (Field field : clazz.instanceFields()) {
                stats.append(maker.Exec(
                        maker.Assign(
                                maker.Select(makeIdent(names._this), field.name()),
                                field.parent() ?
                                        makeIdent(names.parent) : generateRead(field.type(), field.nullable())
                        )
                ));
            }
            for (Field field : clazz.transientFields()) {
                var v = field.init() != null ?
                        copier.copy(field.init()) : makeInitValue(field.type());
                stats.append(maker.Exec(
                        maker.Assign(maker.Select(makeIdent(names._this), field.name()), v)
                ));
            }

        }
        if (clazz.onReadMethod() != null) {
            stats.append(maker.Exec(makeApply(
                    makeSelect(makeThisIdent(), clazz.onReadMethod().name))
            ));
        }
    }

    private JCTree.JCExpression makeInitValue(TypeMirror type) {
        return switch (type.getKind()) {
            case BYTE -> maker.TypeCast(
                    types.byteType,
                    maker.Literal(0)
            );
            case SHORT -> maker.TypeCast(
                    types.shortType,
                    maker.Literal(0)
            );
            case INT -> maker.Literal(0);
            case LONG -> maker.Literal(TypeTag.LONG, 0L);
            case FLOAT -> maker.Literal(TypeTag.FLOAT, 0.0f);
            case DOUBLE -> maker.Literal(TypeTag.DOUBLE, 0.0);
            case CHAR -> maker.Literal('\0');
            case BOOLEAN -> maker.Literal(false);
            default -> makeNullLiteral();
        };
    }

    private JCTree.JCExpression generateRead(TypeMirror type, boolean nullable) {
        if (nullable) {
            return makeRead(methods.readNullable, List.of(
                    maker.Lambda(List.nil(), generateRead(type, false))
            ));
        }
        return switch (type.getKind()) {
            case BYTE -> makeRead(methods.readByte);
            case SHORT -> makeRead(methods.readShort);
            case INT -> makeRead(methods.readInt);
            case LONG -> makeRead(methods.readLong);
            case FLOAT -> makeRead(methods.readFloat);
            case DOUBLE -> makeRead(methods.readDouble);
            case CHAR -> makeRead(methods.readChar);
            case BOOLEAN -> makeRead(methods.readBoolean);
            case DECLARED -> {
                var declaredType = (DeclaredType) type;
                var cl = (Symbol.ClassSymbol) declaredType.asElement();
                if (cl.isEnum()) {
                    var e = introspects.introspectEnum(cl);
                    if (e.fromCodeMethod() != null) {
                        yield makeApply(
                                makeSelect(makeType(cl.type), e.fromCodeMethod().name),
                                List.of(makeRead(methods.readByte))
                        );
                    } else {
                        yield makeApply(
                                makeSelect(makeType(cl.type), names.valueOf),
                                List.of(makeRead(methods.readString))
                        );
                    }
                }
                if (cl == classes._byte)
                    yield makeRead(methods.readByte);
                if (cl == classes._short)
                    yield makeRead(methods.readShort);
                if (cl == classes.integer)
                    yield makeRead(methods.readInt);
                if (cl == classes._long)
                    yield makeRead(methods.readLong);
                if (cl == classes._float)
                    yield makeRead(methods.readFloat);
                if (cl == classes._double)
                    yield makeRead(methods.readDouble);
                if (cl == classes._boolean)
                    yield makeRead(methods.readBoolean);
                if (cl == classes.character)
                    yield makeRead(methods.readChar);
                if (cl == classes.string)
                    yield makeRead(methods.readString);
                if (cl == classes.date)
                    yield makeRead(methods.readDate);
                if (cl == classes.list) {
                    var elementType = declaredType.getTypeArguments().isEmpty() ?
                            types.object : declaredType.getTypeArguments().getFirst();
                    var elemNullable = isAnnotationPresent(elementType, classes.nullable);
                    var arg = maker.Lambda(List.nil(), generateRead(elementType, elemNullable));
                    yield makeRead(methods.readList, List.of(arg));
                }
                var adapterName = Objects.requireNonNull(adapterNames.get(cl), () -> "Adapter not found for type: " + type);
                var parent = clazz.symbol().isRecord() ? makeNullLiteral() : makeThisIdent();
                yield makeRead(methods.readEntity, List.of(maker.Ident(adapterName), parent));
            }
            case ARRAY -> {
                var arrayType = (Type.ArrayType) type;
                var elemType = arrayType.getComponentType();
                if (elemType.getKind() == TypeKind.BYTE)
                    yield makeRead(methods.readBytes);
                var elemNullable = isAnnotationPresent(elemType, classes.nullable);
                var arg = maker.Lambda(List.nil(), generateRead(elemType, elemNullable));
                yield makeRead(methods.readArray, List.of(arg, maker.Reference(
                        MemberReferenceTree.ReferenceMode.NEW,
                        names._init,
                        makeType(arrayType),
                        null
                )));
            }
            default -> throw new UnsupportedOperationException("Unsupported type: " + type);
        };
    }

    private boolean writeExists() {
        var paramTypes = new ListBuffer<Type>();
        paramTypes.append(types.wireOutput);
        clazz.forEachFieldAdapter(adapter -> paramTypes.append(adapter.type()));
        return methods.methodExists(clazz.symbol(), names.__write__, paramTypes.toList());
    }

    private JCTree.JCMethodDecl generateWrite() {
        var stats = new ListBuffer<JCTree.JCStatement>();
        var params = new ListBuffer<JCTree.JCVariableDecl>();
        params.append(makeParam(names.output, makeType(types.wireOutput)));
        clazz.forEachFieldAdapter(adapter -> {
            var paramName = adapterNames.get(adapter.valueType().tsym);
            params.append(makeParam(paramName, adapter.type()));
        });
        if (clazz.superClass() != null) {
            var superArgs = new ListBuffer<JCTree.JCExpression>();
            superArgs.append(makeIdent(names.output));
            clazz.superClass().forEachFieldAdapter(adapter ->
                    superArgs.append(makeIdent(adapterNames.get(adapter.valueType().tsym)))
            );
            stats.append(maker.Exec(makeApply(
                makeSelect(makeIdent(names._super), names.__write__),
                superArgs.toList()
            )));
        }
        for (var field : clazz.instanceFields()) {
            if (field.parent())
                continue;
            stats.append(
                    maker.Exec(generateWrite(maker.Select(makeThisIdent(), field.name()), field.type(), field.nullable()))
            );
        }
        return makeMethod(
                Flags.PROTECTED,
                names.__write__,
                maker.TypeIdent(TypeTag.VOID),
                params.toList(),
                stats.toList()
        );
    }

    private JCTree.JCExpression generateWrite(JCTree.JCExpression value, TypeMirror type, boolean nullable) {
        if (nullable) {
            var v = nextVarName();
            return makeWrite(methods.writeNullable,
                    value,
                    maker.Lambda(
                            List.of(makeParam(v, makeType(type))),
                            generateWrite(maker.Ident(v), type, false)
                    )
            );
        }
        return switch (type.getKind()) {
            case BOOLEAN -> makeWrite(methods.writeBoolean, value);
            case CHAR -> makeWrite(methods.writeChar, value);
            case BYTE -> makeWrite(methods.writeByte, value);
            case SHORT -> makeWrite(methods.writeShort, value);
            case INT -> makeWrite(methods.writeInt, value);
            case LONG -> makeWrite(methods.writeLong, value);
            case FLOAT -> makeWrite(methods.writeFloat, value);
            case DOUBLE -> makeWrite(methods.writeDouble, value);
            case DECLARED -> {
                var declaredType = (Type.ClassType) type;
                var cl = (Symbol.ClassSymbol) declaredType.asElement();
                if (cl.isEnum()) {
                    var e = introspects.introspectEnum(cl);
                    if (e.codeMethod() != null) {
                        yield makeWrite(methods.writeByte, makeTypeCast(
                                types.byteType,
                                makeApply(makeSelect(value, e.codeMethod().name))
                        ));
                    } else {
                        yield makeWrite(methods.writeString, makeApply(
                                makeSelect(value, names.name),
                                List.nil()
                        ));
                    }
                }
                if (cl == classes._byte)
                    yield makeWrite(methods.writeByte, value);
                if (cl == classes._short)
                    yield makeWrite(methods.writeShort, value);
                if (cl == classes.integer)
                    yield makeWrite(methods.writeInt, value);
                if (cl == classes._long)
                    yield makeWrite(methods.writeLong, value);
                if (cl == classes._float)
                    yield makeWrite(methods.writeFloat, value);
                if (cl == classes._double)
                    yield makeWrite(methods.writeDouble, value);
                if (cl == classes._boolean)
                    yield makeWrite(methods.writeBoolean, value);
                if (cl == classes.character)
                    yield makeWrite(methods.writeChar, value);
                if (cl == classes.string)
                    yield makeWrite(methods.writeString, value);
                if (cl == classes.date)
                    yield makeWrite(methods.writeDate, value);
                if (cl == classes.list) {
                    var elementType = declaredType.getTypeArguments().isEmpty() ?
                            types.object : declaredType.getTypeArguments().getFirst();
                    var v = nextVarName();
                    var p = makeParam(v, maker.Type(elementType));
                    var elementNullable = isAnnotationPresent(elementType, classes.nullable);
                    var arg = maker.Lambda(List.of(p), generateWrite(maker.Ident(v), elementType, elementNullable));
                    yield makeWrite(methods.writeList, value, arg);
                }
                var adapterName = adapterNames.get(cl);
                yield makeWrite(methods.writeEntity, value, maker.Ident(adapterName));
            }
            case ARRAY -> {
                var arrayType = (ArrayType) type;
                var elemType = arrayType.getComponentType();
                if (elemType.getKind() == TypeKind.BYTE)
                    yield makeWrite(methods.writeBytes, value);
                var elemNullable = isAnnotationPresent(elemType, classes.nullable);
                var v = nextVarName();
                var p = makeParam(v, elemType);
                var arg = maker.Lambda(List.of(p), generateWrite(makeIdent(v), elemType, elemNullable));
                yield makeWrite(methods.writeArray, value, arg);
            }
            default -> throw new UnsupportedOperationException("Unsupported type: " + type);
        };
    }

    private JCTree.JCExpression makeWrite(Symbol.MethodSymbol method, JCTree.JCExpression arg0, JCTree.JCExpression arg1) {
        return makeApply(maker.Select(maker.Ident(names.output), method), List.of(arg0, arg1));
    }

    private JCTree.JCExpression makeWrite(Symbol.MethodSymbol method, JCTree.JCExpression arg) {
        return makeApply(maker.Select(maker.Ident(names.output), method), List.of(arg));
    }

    private JCTree.JCExpression makeRead(Symbol.MethodSymbol method) {
        return makeRead(method, List.nil());
    }

    private JCTree.JCExpression makeRead(Symbol.MethodSymbol method, List<JCTree.JCExpression> args) {
        next();
        return maker.Apply(List.nil(),
                maker.Select(
                        maker.Ident(names.input),
                        method
                ),
                args
        );
    }

    private boolean visitExists() {
        var paramTypes = new ListBuffer<Type>();
        paramTypes.append(types.wireVisitor);
        clazz.forEachFieldAdapter(adapter -> paramTypes.append(adapter.type()));
        return methods.methodExists(clazz.symbol(), names.__visit__, paramTypes.toList());
    }

    private JCTree.JCMethodDecl generateVisit() {
        var params = new ListBuffer<JCTree.JCVariableDecl>();
        params.append(makeParam(names.visitor, makeType(types.wireVisitor)));
        clazz.forEachFieldAdapter(adapter -> params.append(makeParam(adapterNames.get(adapter.valueType().tsym), adapter.type())));
        var stats = new ListBuffer<JCTree.JCStatement>();
        if (clazz.superClass() != null) {
            var superArgs = new ListBuffer<JCTree.JCExpression>();
            superArgs.append(makeIdent(names.visitor));
            clazz.superClass().forEachFieldAdapter(adapter -> superArgs.append(makeIdent(adapterNames.get(adapter.valueType().tsym))));
            stats.append(maker.Exec(makeApply(
                    makeSelect(makeType(clazz.superClass().symbol().type), names.__visit__), superArgs.toList()
            )));
        }
        for (var field : clazz.instanceFields()) {
            if (field.parent())
                continue;
            stats.append(maker.Exec(generateVisit(field.type(), field.nullable())));
        }
        return makeMethod(
                Flags.PROTECTED | Flags.STATIC,
                names.__visit__,
                maker.TypeIdent(TypeTag.VOID),
                params.toList(),
                stats.toList()
        );
    }

    private JCTree.JCExpression generateVisit(TypeMirror type, boolean nullable) {
        if (nullable) {
            return makeVisit(methods.visitNullable, List.of(
                    maker.Lambda(List.nil(), generateVisit(type, false))
            ));
        }
        return switch (type.getKind()) {
            case BYTE -> makeVisit(methods.visitByte);
            case SHORT -> makeVisit(methods.visitShort);
            case INT -> makeVisit(methods.visitInt);
            case LONG -> makeVisit(methods.visitLong);
            case FLOAT -> makeVisit(methods.visitFloat);
            case DOUBLE -> makeVisit(methods.visitDouble);
            case CHAR -> makeVisit(methods.visitChar);
            case BOOLEAN -> makeVisit(methods.visitBoolean);
            case DECLARED -> {
                var declaredType = (DeclaredType) type;
                var cl = (Symbol.ClassSymbol) declaredType.asElement();
                if (cl.isEnum()) {
                    var e = introspects.introspectEnum(cl);
                    if (e.codeMethod() != null)
                        yield makeVisit(methods.visitByte);
                    else
                        yield makeVisit(methods.visitString);
                }
                if (cl == classes._byte)
                    yield makeVisit(methods.visitByte);
                if (cl == classes._short)
                    yield makeVisit(methods.visitShort);
                if (cl == classes.integer)
                    yield makeVisit(methods.visitInt);
                if (cl == classes._long)
                    yield makeVisit(methods.visitLong);
                if (cl == classes._float)
                    yield makeVisit(methods.visitFloat);
                if (cl == classes._double)
                    yield makeVisit(methods.visitDouble);
                if (cl == classes._boolean)
                    yield makeVisit(methods.visitBoolean);
                if (cl == classes.character)
                    yield makeVisit(methods.visitChar);
                if (cl == classes.string)
                    yield makeVisit(methods.visitString);
                if (cl == classes.date)
                    yield makeVisit(methods.visitDate);
                if (cl == classes.list) {
                    var elementType = declaredType.getTypeArguments().isEmpty() ?
                            types.object : declaredType.getTypeArguments().getFirst();
                    var elemNullable = isAnnotationPresent(elementType, classes.nullable);
                    var arg = maker.Lambda(List.nil(), generateVisit(elementType, elemNullable));
                    yield makeVisit(methods.visitList, List.of(arg));
                }
                var adapterName = adapterNames.get(cl);
                yield makeVisit(methods.visitEntity, List.of(maker.Ident(adapterName)));
            }
            case ARRAY -> {
                var arrayType = (ArrayType) type;
                var elementType = arrayType.getComponentType();
                if (elementType.getKind() == TypeKind.BYTE)
                    yield makeVisit(methods.visitBytes);
                var elemNullable = isAnnotationPresent(elementType, classes.nullable);
                var arg = maker.Lambda(List.nil(), generateVisit(elementType, elemNullable));
                yield makeVisit(methods.visitArray, List.of(arg));
            }
            default -> throw new UnsupportedOperationException("Unsupported type: " + type);
        };
    }

    private JCTree.JCExpression makeVisit(Symbol.MethodSymbol method) {
        return makeVisit(method, List.nil());
    }

    private JCTree.JCExpression makeVisit(Symbol.MethodSymbol method, List<JCTree.JCExpression> args) {
        return maker.Apply(List.nil(),
                maker.Select(
                        maker.Ident(names.visitor),
                        method
                ),
                args
        );
    }

    private Name nextAdapterName() {
        return names.fromString("adapter" + nextAdapterIndex++);
    }

    /**
     * Clear the GENERATEDCONSTR flag of the default constructor to ensure it's not removed
     */
    private void clearDefaultConstrFlag() {
        for (Symbol member : clazz.symbol().getEnclosedElements()) {
            if (member.getKind() == ElementKind.CONSTRUCTOR) {
                var m = (JCTree.JCMethodDecl) trees.getTree(member);
                if ((m.mods.flags & Flags.GENERATEDCONSTR) != 0) {
                    m.mods.flags &= ~Flags.GENERATEDCONSTR;
                }
            }
        }
    }

}
