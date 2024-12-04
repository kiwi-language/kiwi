package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.Element;
import org.metavm.entity.EntityRepository;
import org.metavm.entity.GenericDeclaration;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;
import org.metavm.util.*;

import java.io.InputStream;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public class KlassInput extends MvInput {

    public static final String UNNAMED = "<unnamed>";
    private static final Klass KLASS = DummyKlass.INSTANCE;

    private final EntityRepository repository;
    private final LinkedList<Element> elements = new LinkedList<>();

    public KlassInput(InputStream in, EntityRepository repository) {
        super(in);
        this.repository = repository;
    }

    public Klass readKlass() {
        var klass = getKlass(readId());
        setKlassParent(klass);
        enterElement(klass);
        klass.read(this);
        exitElement();
        return klass;
    }

    protected void setKlassParent(Klass klass) {
        var parent = elements.peek();
        switch (parent) {
            case null -> {}
            case Flow f -> klass.setEnclosingFlow(f);
            case Klass k -> klass.setDeclaringKlass(k);
            default -> throw new IllegalStateException("Invalid enclosing element for klass: " + parent);
        }
    }

    @Override
    public Klass getKlass(Id id) {
        var klass = repository.getEntity(Klass.class, id);
        if(klass == null)
            klass = repository.bind(KlassBuilder.newBuilder(UNNAMED, UNNAMED).tmpId(id.tmpId()).build());
        return klass;
    }

    public Method readMethod() {
        var method = getMethod(readId());
        method.setDeclaringType((Klass) currentElement(), false);
        enterElement(method);
        method.read(this);
        exitElement();
        return method;
    }

    @Override
    public Method getMethod(Id id) {
        var method = repository.getEntity(Method.class, id);
        if(method == null)
            method = repository.bind(MethodBuilder.newBuilder(KLASS, UNNAMED).tmpId(id.tmpId()).build());
        return method;
    }

    public Type readType() {
        return Type.readType(this);
    }

    public Field readField() {
        var field = getField(readId());
        field.setDeclaringType((Klass) currentElement());
        enterElement(field);
        field.read(this);
        exitElement();
        return field;
    }

    @Override
    public Field getField(Id id) {
        var field = repository.getEntity(Field.class, id);
        if(field == null)
            field = repository.bind(FieldBuilder.newBuilder(UNNAMED, KLASS, Types.getAnyType())
                    .column(ColumnKind.getColumnByName("r0"))
                    .tmpId(id.tmpId()).build());
        return field;
    }

    public TypeVariable readTypeVariable() {
        var typeVar = getTypeVariable(readId());
        typeVar.setGenericDeclaration((GenericDeclaration) currentElement());
        enterElement(typeVar);
        typeVar.read(this);
        exitElement();
        return typeVar;
    }

    @Override
    public TypeVariable getTypeVariable(Id id) {
        var typeVar = repository.getEntity(TypeVariable.class, id);
        if(typeVar == null)
            typeVar = repository.bind(new TypeVariable(id.tmpId(), UNNAMED, DummyGenericDeclaration.INSTANCE));
        return typeVar;
    }

    @Override
    public Function getFunction(Id id) {
        var func = repository.getEntity(Function.class, id);
        if(func == null)
            func = repository.bind(FunctionBuilder.newBuilder(UNNAMED).tmpId(id.tmpId()).build());
        return func;
    }

    public CapturedTypeVariable readCapturedTypeVariable() {
        var capturedTypeVar = getCapturedTypeVariable(readId());
        capturedTypeVar.setScope((CapturedTypeScope) currentElement());
        enterElement(capturedTypeVar);
        capturedTypeVar.read(this);
        exitElement();
        return capturedTypeVar;
    }

    @Override
    public CapturedTypeVariable getCapturedTypeVariable(Id id) {
        var capturedTypeVar = repository.getEntity(CapturedTypeVariable.class, id);
        if(capturedTypeVar == null)
            capturedTypeVar = repository.bind(new CapturedTypeVariable(id.tmpId(), UncertainType.asterisk,
                    DummyCapturedTypeScope.INSTANCE));
        return capturedTypeVar;
    }

    public Parameter readParameter() {
        var param = getParameter(readId());
        param.setCallable((Callable) currentElement());
        enterElement(param);
        param.read(this);
        exitElement();
        return param;
    }

    public Parameter getParameter(Id id) {
        var param = repository.getEntity(Parameter.class, id);
        if(param == null)
            param = repository.bind(new Parameter(id.tmpId(), UNNAMED, Types.getAnyType(), DummyCallable.INSTANCE));
        return param;
    }

    public Lambda readLambda() {
        var lambda = getLambda(readId());
        lambda.setFlow((Flow) currentElement());
        enterElement(lambda);
        lambda.read(this);
        exitElement();
        return lambda;
    }

    @Override
    public Lambda getLambda(Id id) {
        var lambda = repository.getEntity(Lambda.class, id);
        if (lambda == null)
            lambda = repository.bind(new Lambda(id.tmpId(), List.of(), Types.getVoidType(), DummyMethod.INSTANCE));
        return lambda;
    }

    public Index readIndex() {
        var index = getIndex(readId());
        index.setDeclaringType((Klass) currentElement());
        enterElement(index);
        index.read(this);
        exitElement();
        return index;
    }

    @Override
    public Index getIndex(Id id) {
        var index = repository.getEntity(Index.class, id);
        if(index == null)
            index = repository.bind(new Index(id.tmpId(), KLASS, UNNAMED, "", false));
        return index;
    }

    public IndexField readIndexField() {
        var indexField = getIndexField(readId());
        indexField.setIndex((Index) currentElement());
        enterElement(indexField);
        indexField.read(this);
        exitElement();
        return indexField;
    }

    @Override
    public IndexField getIndexField(Id id) {
        var indexField = repository.getEntity(IndexField.class, id);
        if (indexField == null) {
            indexField = new IndexField(DummyIndex.INSTANCE, UNNAMED, Values.nullValue());
            indexField.setTmpId(id.tmpId());
            repository.bind(indexField);
        }
        return indexField;
    }

    public EnumConstantDef readEnumConstantDef() {
        var ecd = getEnumConstantDef(readId());
        ecd.setKlass((Klass) currentElement());
        enterElement(ecd);
        ecd.read(this);
        exitElement();
        return ecd;
    }

    @Override
    public EnumConstantDef getEnumConstantDef(Id id) {
        var ecd = repository.getEntity(EnumConstantDef.class, id);
        if (ecd == null) {
            ecd = new EnumConstantDef(KLASS, UNNAMED, 0, DummyMethod.INSTANCE);
            ecd.setTmpId(id.tmpId());
            repository.bind(ecd);
        }
        return ecd;
    }

    public Column readColumn() {
        return new Column(ColumnKind.fromTagSuffix(read()), readUTF(), readInt());
    }

    public Object readElement() {
        var tag = read();
        if(tag >= WireTypes.CLASS_TYPE && tag < 50)
            return Type.readType(tag, this);
        return switch (tag) {
            case WireTypes.NULL -> Instances.nullInstance();
            case WireTypes.LONG -> Instances.longInstance(readLong());
            case WireTypes.INT -> Instances.intInstance(readInt());
            case WireTypes.DOUBLE -> Instances.doubleInstance(readDouble());
            case WireTypes.FLOAT -> Instances.floatInstance(readFloat());
            case WireTypes.STRING -> Instances.stringInstance(readUTF());
            case WireTypes.CHAR -> Instances.charInstance(readChar());
            case WireTypes.BOOLEAN -> Instances.booleanInstance(readBoolean());
            case WireTypes.TIME -> Instances.timeInstance(readLong());
            case WireTypes.PASSWORD -> Instances.passwordInstance(readUTF());
            case WireTypes.FIELD_REF -> FieldRef.read(this);
            case WireTypes.METHOD_REF -> MethodRef.read(this);
            case WireTypes.FUNCTION_REF -> FunctionRef.read(this);
            case WireTypes.INDEX_REF -> IndexRef.read(this);
            case WireTypes.LAMBDA_REF -> LambdaRef.read(this);
            default -> throw new IllegalStateException("Invalid value tag: " + tag);
        };
    }

    protected void enterElement(Element element) {
        elements.push(element);
    }

    protected void exitElement() {
        elements.pop();
    }

    protected Element currentElement() {
        return requireNonNull(elements.peek());
    }

}
