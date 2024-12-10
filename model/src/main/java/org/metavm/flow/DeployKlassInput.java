package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.type.*;
import org.metavm.util.Constants;

import java.io.InputStream;
import java.util.Objects;

@Slf4j
public class DeployKlassInput extends KlassInput {

    private final SaveTypeBatch batch;

    public DeployKlassInput(InputStream in, SaveTypeBatch batch) {
        super(in, batch.getContext());
        this.batch = batch;
    }

    @Override
    public Klass readKlass() {
        var klass = getKlass(readId());
        setKlassParent(klass);
        var oldKind = klass.getKind();
        var oldSuperType = klass.getSuperType();
        var oldSearchable = klass.isSearchable();
        enterElement(klass);
        klass.read(this);
        exitElement();
        var context = batch.getContext();
        batch.addKlass(klass);
        var newKind = klass.getKind();
        if(context.isNewEntity(klass)) {
            if(klass.getTag() == TypeTags.DEFAULT)
                klass.setTag(KlassTagAssigner.getInstance(context).next());
            if(klass.getSourceTag() == null)
                klass.setSourceTag(KlassSourceCodeTagAssigner.getInstance(context).next());
        } else {
            if(newKind != oldKind) {
                if(newKind == ClassKind.VALUE)
                    batch.addEntityToValueKlass(klass);
                else if (oldKind == ClassKind.VALUE)
                    batch.addValueToEntityKlass(klass);
                if(newKind == ClassKind.ENUM)
                    batch.addToEnumKlass(klass);
                else if(oldKind == ClassKind.ENUM)
                    batch.addFromEnumKlass(klass);
            }
            if(!klass.isEnum() && klass.getSuperType() != null && !Objects.equals(oldSuperType, klass.getSuperType()))
                batch.addChangingSuperKlass(klass);
            if (klass.isSearchable() && !oldSearchable)
                batch.addSearchEnabledKlass(klass);
        }
        context.update(klass);
        return klass;
    }

    @Override
    public Field readField() {
        var field = getField(readId());
        var declaringType = (Klass) currentElement();
        field.setDeclaringType(declaringType);
        var oldType = field.getType();
        var oldState = field.getState();
        var oldChild = field.isChild();
        enterElement(field);
        field.read(this);
        exitElement();
        var context = batch.getContext();
        if(context.isNewEntity(field)) {
            field.initTag(declaringType.nextFieldTag());
            if(field.getSourceTag() == null)
                field.setSourceTag(declaringType.nextFieldSourceCodeTag());
            if(!field.isStatic() && !context.isNewEntity(declaringType))
                batch.addNewField(field);
        } else {
            if(!field.isStatic() && !field.getType().isAssignableFrom(oldType)) {
                batch.addTypeChangedField(field);
                field.setTag(field.getDeclaringType().nextFieldTag());
            }
            if(oldState != field.getState()) {
                if(oldState == MetadataState.REMOVED)
                    batch.addNewField(field);
                if(field.getState() == MetadataState.REMOVED && field.isChild())
                    batch.addRemovedChildField(field);
            }
            if(oldChild != field.isChild()) {
                if(field.isChild())
                    batch.addToChildField(field);
                else
                    batch.addToNonChildField(field);
            }
        }
        return field;
    }

    @Override
    public EnumConstantDef readEnumConstantDef() {
        var ecd = getEnumConstantDef(readId());
        ecd.setKlass((Klass) currentElement());
        var oldName = ecd.getName();
        var oldOrdinal = ecd.getOrdinal();
        enterElement(ecd);
        ecd.read(this);
        exitElement();
        var context = batch.getContext();
        if(context.isNewEntity(ecd))
            batch.addNewEnumConstantDef(ecd);
        else if(!ecd.getName().equals(oldName) || ecd.getOrdinal() != oldOrdinal)
            batch.addChangedEnumConstantDef(ecd);
        return ecd;
    }

    @Override
    public Method readMethod() {
        var method = super.readMethod();
        if(method.getParameters().isEmpty() && !method.isStatic() && method.getName().equals(Constants.RUN_METHOD_NAME))
            batch.addRunMethod(method);
        return method;
    }

    @Override
    public Index readIndex() {
        var index = super.readIndex();
        var context = batch.getContext();
        if (context.isNewEntity(index) && !context.isNewEntity(index.getDeclaringType()))
            batch.addNewIndex(index);
        return index;
    }
}
