package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.Entity;
import org.metavm.object.type.*;
import org.metavm.util.Constants;

import javax.annotation.Nullable;
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
    public <T extends Entity> T readEntity(Class<T> klass, Entity parent) {
        if (klass == Klass.class)
            //noinspection unchecked
            return (T) readKlass(parent);
        else if (klass == Field.class)
            //noinspection unchecked
            return (T) readField((Klass) parent);
        else if (klass == Method.class)
            //noinspection unchecked
            return (T) readMethod((Klass) parent);
        else if (klass == Index.class)
            //noinspection unchecked
            return (T) readIndex((Klass) parent);
        else
            return super.readEntity(klass, parent);
    }

    private Klass readKlass(@Nullable Entity parent) {
        var klass = getEntity(Klass.class, readId());
        var context = batch.getContext();
        List<Field> prevEnumConstants = klass.isEnum() && klass.isPersisted() ?
                new ArrayList<>(klass.getEnumConstants()) :
                List.of();
        var oldKind = klass.getKind();
        var oldSuperType = klass.getSuperType();
        var oldSearchable = klass.isSearchable();
        klass.readHeadAndBody(this, parent);
        batch.addKlass(klass);
        var newKind = klass.getKind();
        if(klass.isNew()) {
            context.bind(klass);
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
        return klass;
    }

    private Field readField(Klass declaringType) {
        var field = getEntity(Field.class, readId());
        var prevType = field.getType();
        var prevState = field.getState();
        var prevIsChild = field.isChild();
        var prevName = field.getName();
        var prevOrdinal = field.getOrdinal();
        field.readHeadAndBody(this, declaringType);
        if(field.isNew()) {
            batch.getContext().bind(field);
            field.initTag(declaringType.nextFieldTag());
            if(field.getSourceTag() == null)
                field.setSourceTag(declaringType.nextFieldSourceCodeTag());
            if(field.isStatic())
                batch.addNewStaticField(field);
            else if (declaringType.isPersisted())
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

    private Method readMethod(Klass declaringKlass) {
        var method = super.readEntity(Method.class, declaringKlass);
        if(method.getParameters().isEmpty() && !method.isStatic() && method.getName().equals(Constants.RUN_METHOD_NAME))
            batch.addRunMethod(method);
        return method;
    }

    private Index readIndex(Klass declaringKlass) {
        var index = super.readEntity(Index.class, declaringKlass);
        if (index.isNew())
            batch.addNewIndex(index);
        return index;
    }
}
