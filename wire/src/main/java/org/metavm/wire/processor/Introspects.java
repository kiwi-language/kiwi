package org.metavm.wire.processor;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import org.metavm.wire.Wire;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.*;

import static java.util.Objects.requireNonNull;

class Introspects {

    private final MyTypes myTypes;
    private final Trees trees;
    private final MyNames names;
    private final MyClasses myClasses;
    private final MyTypes types;
    private final Map<Symbol.ClassSymbol, Clazz> classes = new HashMap<>();
    private final Map<Symbol.ClassSymbol, Enum> enums = new HashMap<>();

    public Introspects(MyTypes myTypes, Trees trees, MyNames names, MyClasses myClasses, MyTypes types) {
        this.myTypes = myTypes;
        this.trees = trees;
        this.names = names;
        this.myClasses = myClasses;
        this.types = types;
    }

    Clazz introspectEntity(Symbol.ClassSymbol symbol) {
        var existing = classes.get(symbol);
        if (existing != null)
            return existing;
        Clazz superClazz = null;
        var nextAdapterIndex = 0;
        if (symbol.getSuperclass().getTag() != TypeTag.NONE && !isIgnoredSuperType(symbol.getSuperclass())) {
            var superCls = (Symbol.ClassSymbol) symbol.getSuperclass().asElement();
            if (superCls.getAnnotation(Wire.class) == null)
                throw new WireConfigException("Superclass " + superCls.getQualifiedName() + " of compiled entity " + symbol.getQualifiedName() + " is not annotated with @Wire.", symbol);
            superClazz = introspectEntity(superCls);
            nextAdapterIndex = superClazz.getNumAdapters();
        }
        var fields = new ListBuffer<Field>();
        var transientFields = new ListBuffer<Field>();
        var adapters = new LinkedHashMap<Symbol.TypeSymbol, Adapter>();
        Field parentField = null;
        Symbol.MethodSymbol onReadMethod = null;
        for (Symbol member : symbol.getEnclosedElements()) {
            if (member.getKind() == ElementKind.FIELD
                    && !member.getModifiers().contains(Modifier.STATIC)
            ) {
                var transi = member.getModifiers().contains(Modifier.TRANSIENT);
                var type = member.asType();
                Name adapterName = null;
                var valueType = getValueType(type);
                var isParent = Annotations.isAnnotationPresent(member, myClasses.parent);
                if (isReferenceType(valueType)
                        && !transi
                        && !isParent
                        && (superClazz == null || superClazz.findAdapter(valueType.tsym) == null || !superClazz.getAdapter(valueType.tsym).forField)
                        && !adapters.containsKey(valueType.tsym)) {
                    adapterName = names.fromString("adapter" + nextAdapterIndex++);
                    var adapterType = (Type.ClassType) types.getDeclaredType(
                            (TypeElement) types.wireAdapter.asElement(), valueType
                    );
                    var adapter = new Adapter(adapterName, adapterType, (Type.ClassType) valueType);
                    adapter.forField = true;
                    adapters.put(valueType.tsym, adapter);
                }
                var nullable = Annotations.isAnnotationPresent(member, myClasses.nullable);
                var tree = (JCTree.JCVariableDecl) trees.getTree(member);
                var field = new Field(
                        (Symbol.VarSymbol) member,
                        member.name,
                        type,
                        valueType,
                        nullable,
                        isParent,
                        adapterName,
                        tree != null ? tree.init : null
                );
                if (transi)
                    transientFields.append(field);
                else
                    fields.append(field);
                if (isParent)
                    parentField = field;
            }
            else if (member instanceof Symbol.MethodSymbol method && !member.getModifiers().contains(Modifier.STATIC)
                    && member.getSimpleName() == names.onRead
                    && method.getParameters().isEmpty()
            ) {
                onReadMethod = method;
            }
        }

        var subTypes = new ListBuffer<SubType>();
        //noinspection unchecked
        var subTypeAnnotations = (List<AnnotationValue>) Annotations.getAttribute(symbol, myClasses.wire, names.subTypes);
        if (subTypeAnnotations != null) {
            for (AnnotationValue v : subTypeAnnotations) {
                var at = (AnnotationMirror) v;
                var tag = (int) requireNonNull(Annotations.getAttribute(at, names.value));
                var type = (Type.ClassType) requireNonNull(Annotations.getAttribute(at, names.type));
                subTypes.append(new SubType(tag, (Symbol.ClassSymbol) type.tsym));
                if (type.tsym == symbol)
                    continue;
                var existingAdapter = adapters.get(type.tsym);
                if (existingAdapter != null)
                    existingAdapter.forSubType = true;
                else {
                    var adapterName = names.fromString("adapter" + nextAdapterIndex++);
                    var adapter = new Adapter(
                            adapterName,
                            myTypes.getDeclaredType(myClasses.wireAdapter, type),
                            type
                    );
                    adapter.forSubType = true;
                    adapters.put(type.tsym, adapter);
                }
            }
        }
        var clazz = new Clazz(symbol,
                (JCTree.JCClassDecl) trees.getTree(symbol),
                superClazz,
                fields.toList(),
                transientFields.toList(),
                parentField,
                adapters,
                onReadMethod,
                sortSubtypes(subTypes.toList())
                );
        this.classes.put(symbol, clazz);
        return clazz;
    }

    private com.sun.tools.javac.util.List<SubType> sortSubtypes(List<SubType> subTypes) {
        var sorted = new LinkedList<SubType>();
        out: for (SubType subType : subTypes) {
            var it = sorted.listIterator();
            while (it.hasNext()) {
                if (types.isAssignable(subType.symbol().type, it.next().symbol().type)) {
                    it.previous();
                    it.add(subType);
                    continue out;
                }
            }
            sorted.add(subType);
        }
        var buf = new ListBuffer<SubType>();
        for (SubType subType : sorted) {
            buf.append(subType);
        }
        return buf.toList();
    }

    private boolean isReferenceType(Type type) {
        return type instanceof Type.ClassType
                && !myTypes.isSameType(type, myTypes.string)
                && !myTypes.isSameType(type, myTypes.date)
                && !type.tsym.isEnum()
                && !myClasses.isPrimitiveWrapper((Symbol.ClassSymbol) type.tsym);
    }

    Enum introspectEnum(Symbol.ClassSymbol symbol) {
        assert symbol.isEnum();
        var existing = enums.get(symbol);
        if (existing != null)
            return existing;
        Symbol.MethodSymbol codeMethod = null;
        Symbol.MethodSymbol fromCodeMethod = null;
        for (Symbol s : symbol.getEnclosedElements()) {
            if (s.getKind() == ElementKind.METHOD && s.getModifiers().contains(Modifier.PUBLIC)) {
                var m = (Symbol.MethodSymbol) s;
//                if (Annotations.isAnnotationPresent(s, myClasses.enumCode)) {
                if (s.name == names.code && m.getParameters().isEmpty() && m.getReturnType().getTag() == TypeTag.INT) {
//                    if (m.getReturnType().getTag() != TypeTag.INT)
//                        throw new EntityConfigException("@EnumCode method must return int, but found: " + m.getReturnType(), s);
//                    if (m.getParameters().nonEmpty())
//                        throw new EntityConfigException("@EnumCode method must not have parameters.", s);
                    codeMethod = m;
                }
//                else if (Annotations.isAnnotationPresent(s, myClasses.enumFromCode)) {
                else if (m.name == names.fromCode && s.getModifiers().contains(Modifier.STATIC)
                        && m.params.size() == 1  && m.params.head.asType().getTag() == TypeTag.INT
                        && types.isSameType(m.getReturnType(), symbol.asType())) {
//                    if (!types.isSameType(m.getReturnType(), symbol.type))
//                        throw new EntityConfigException("@EnumFromCode method must return enum type " + symbol.getQualifiedName() + ", but found: " + m.getReturnType(), s);
//                    if (m.getParameters().size() != 1 || m.getParameters().head.type.getTag() != TypeTag.INT)
//                        throw new EntityConfigException("@EnumFromCode method must have a single int parameter.", s);
                    fromCodeMethod = m;
                }
            }
        }
        if (codeMethod == null || fromCodeMethod == null)
            throw new WireConfigException("Missing method code or fromCode", symbol);
        var e = new Enum(symbol, codeMethod, fromCodeMethod);
        enums.put(symbol, e);
        return e;
    }

    private Type getValueType(Type type) {
        return switch (type) {
            case Type.ArrayType arrayType -> getValueType(arrayType.getComponentType());
            case Type.ClassType classType -> {
                var cl = classType.tsym;
                if (cl == myClasses.list) {
                    var elemType = classType.getTypeArguments().isEmpty() ? myTypes.object : classType.getTypeArguments().head;
                    yield getValueType(elemType);
                } else
                    yield type;
            }
            default -> type;
        };
    }

    private boolean isIgnoredSuperType(TypeMirror superType) {
        return myTypes.isSameType(superType, myTypes.object) || myTypes.isSameType(superType, myTypes.record);
    }

}
