package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.type.*;
import org.metavm.util.Constants;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        var context = batch.getContext();
        List<Field> prevEnumConstants = klass.isEnum() && !context.isNewEntity(klass) ?
                new ArrayList<>(klass.getEnumConstants()) :
                List.of();
        setKlassParent(klass);
        var oldKind = klass.getKind();
        var oldSuperType = klass.getSuperType();
        var oldSearchable = klass.isSearchable();
        enterElement(klass);
        klass.read(this);
        exitElement();
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
            if (klass.isEnum()) {
                var enumConstantSet = new HashSet<>(klass.getEnumConstants());
                for (var prevEnumConstant : prevEnumConstants) {
                    if (!enumConstantSet.contains(prevEnumConstant))
                        batch.addRemovedEnumConstant(prevEnumConstant);
                }
            }
        }
        context.update(klass);
        return klass;
    }

    @Override
    public Field readField() {
        var field = getField(readId());
        var declaringType = (Klass) currentElement();
        field.setDeclaringType(declaringType);
        var prevType = field.getType();
        var prevState = field.getState();
        var prevIsChild = field.isChild();
        var prevName = field.getName();
        var prevOrdinal = field.getOrdinal();
        enterElement(field);
        field.read(this);
        exitElement();
        var context = batch.getContext();
        if(context.isNewEntity(field)) {
            field.initTag(declaringType.nextFieldTag());
            if(field.getSourceTag() == null)
                field.setSourceTag(declaringType.nextFieldSourceCodeTag());
            if(field.isStatic())
                batch.addNewStaticField(field);
            else if (!context.isNewEntity(declaringType))
                batch.addNewField(field);
            if (field.isEnumConstant())
                batch.addNewEnumConstant(field);
        } else {
            if(!field.isStatic() && !field.getType().isAssignableFrom(prevType)) {
                batch.addTypeChangedField(field);
                field.setTag(field.getDeclaringType().nextFieldTag());
            }
            if(prevState != field.getState()) {
                if(prevState == MetadataState.REMOVED) {
                    if (field.isStatic())
                        batch.addNewStaticField(field);
                    else
                        batch.addNewField(field);
                }
                if(field.getState() == MetadataState.REMOVED && field.isChild())
                    batch.addRemovedChildField(field);
            }
            if(prevIsChild != field.isChild()) {
                if(field.isChild())
                    batch.addToChildField(field);
                else
                    batch.addToNonChildField(field);
            }
            if (field.isEnumConstant() && (!prevName.equals(field.getName()) || prevOrdinal != field.getOrdinal()))
                batch.addModifiedEnumConstant(field);
        }
        return field;
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
        if (context.isNewEntity(index))
            batch.addNewIndex(index);
        return index;
    }
}
