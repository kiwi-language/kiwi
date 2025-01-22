package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.Entity;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.*;
import org.metavm.util.Constants;
import org.metavm.util.DebugEnv;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Slf4j
public class DeployKlassInput extends KlassInput {

    private final SaveTypeBatch batch;
    private @Nullable KlassInfo klassInfo;

    private final boolean tracing = DebugEnv.traceDeployment;

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
            return readEntity0(klass, parent);
    }

    private <T extends Entity> T readEntity0(Class<T> klass, Entity parent) {
        var entity = getOrCreateEntity(klass);
        enterSymbolMap(entity);
        entity.readHeadAndBody(this, parent);
        exitSymbolMap();
        var context = batch.getContext();
        if (entity.tryGetId() instanceof TmpId tmpId && context.getEntity(klass, tmpId) == null)
            context.bind(entity);
        else 
            context.updateMemoryIndex(entity);
        return entity;
    }

    private Klass readKlass(@Nullable Entity parent) {
        var klass = getOrCreateEntity(Klass.class);
        var tracing = this.tracing;
        var context = batch.getContext();
        List<Field> prevEnumConstants = klass.isEnum() && klass.isPersisted() ?
                new ArrayList<>(klass.getEnumConstants()) :
                List.of();
        var prevKind = klass.getKind();
        var pevSuperType = klass.getSuperType();
        var prevSearchable = klass.isSearchable();
        var preTag = klass.getTag();
        var prevSourceTag = klass.getSourceTag();
        var prevKlassInfo = klassInfo;
        klassInfo = new KlassInfo(klass.getNextFieldTag());
        if (tracing && !klass.isNew())
            log.trace("Reading existing klass {}, next field tag: {}", klass.getName(), klass.getNextFieldTag());
        List<Field> oldFields = klass.isNew() ? List.of() : klass.getFields();
        enterSymbolMap(klass);
        if (tracing)
            logCurrentSymbols();
        klass.readHeadAndBody(this, parent);
        exitSymbolMap();
        klass.setNextFieldTag(klassInfo.nextFieldTag);
        klassInfo = prevKlassInfo;
        batch.addKlass(klass);
        var newKind = klass.getKind();
        if(klass.isNew()) {
            if (tracing) log.trace("Read new klass {}", klass.getName());
            context.bind(klass);
            if(klass.getTag() == TypeTags.DEFAULT) {
                klass.setTag(KlassTagAssigner.getInstance(context).next());
                if (tracing) log.trace("Assigned tag {} to klass {}", klass.getTag(), klass.getName());
            } if(klass.getSourceTag() == null)
                klass.setSourceTag(KlassSourceCodeTagAssigner.getInstance(context).next());
        } else {
            klass.setTag(preTag);
            klass.setSourceTag(prevSourceTag);
            if (tracing) log.trace("Read existing klass {}", klass.getName());
            if(newKind != prevKind) {
                if(newKind == ClassKind.VALUE)
                    batch.addEntityToValueKlass(klass);
                else if (prevKind == ClassKind.VALUE)
                    batch.addValueToEntityKlass(klass);
                if(newKind == ClassKind.ENUM)
                    batch.addToEnumKlass(klass);
                else if(prevKind == ClassKind.ENUM)
                    batch.addFromEnumKlass(klass);
            }
            if(!klass.isEnum() && klass.getSuperType() != null && !Objects.equals(pevSuperType, klass.getSuperType())) {
                batch.addChangingSuperKlass(klass);
            } if (klass.isSearchable() && !prevSearchable)
                batch.addSearchEnabledKlass(klass);
            if (klass.isEnum()) {
                var enumConstantSet = new HashSet<>(klass.getEnumConstants());
                for (var prevEnumConstant : prevEnumConstants) {
                    if (!enumConstantSet.contains(prevEnumConstant))
                        batch.addRemovedEnumConstant(prevEnumConstant);
                }
            }
            if (!oldFields.isEmpty()) {
                var fieldSet = new HashSet<>(klass.getFields());
                for (var oldField : oldFields) {
                    if (!fieldSet.contains(oldField)) {
                        if (oldField.isChild()) {
                            batch.addRemovedChildField(
                                    FieldBuilder.newBuilder(oldField.getName(), klass, oldField.getType())
                                            .isChild(true)
                                            .tag(oldField.getTag())
                                            .state(MetadataState.REMOVED)
                                            .build()
                            );
                        }
                    }
                }
            }
            context.updateMemoryIndex(klass);
        }
        return klass;
    }

    private Field readField(Klass declaringType) {
        var tracing = this.tracing;
        var field = getOrCreateEntity(Field.class);
        var prevType = field.getType();
        var prevState = field.getState();
        var prevIsChild = field.isChild();
        var prevName = field.getName();
        var prevOrdinal = field.getOrdinal();
        var pevTag = field.getTag();
        var prevSourceTag = field.getSourceTag();
        enterSymbolMap(field);
        field.readHeadAndBody(this, declaringType);
        exitSymbolMap();
        if(field.isNew()) {
            if (tracing)
                log.trace("Reading new field {}", field.getQualifiedName());
            batch.getContext().bind(field);
            field.initTag(Objects.requireNonNull(klassInfo).nextFieldTag());
            if(field.getSourceTag() == null)
                field.setSourceTag(declaringType.nextFieldSourceCodeTag());
            if(field.isStatic())
                batch.addNewStaticField(field);
            else if (declaringType.isPersisted())
                batch.addNewField(field);
            if (field.isEnumConstant())
                batch.addNewEnumConstant(field);
        } else {
            field.setTag(pevTag);
            if(!field.isStatic() && !field.getType().isAssignableFrom(prevType)) {
                batch.addTypeChangedField(field);
                field.changeTag(Objects.requireNonNull(klassInfo).nextFieldTag());
            }
            field.setSourceTag(prevSourceTag);
            if(prevState != field.getState()) {
                if(prevState == MetadataState.REMOVED) {
                    if (field.isStatic())
                        batch.addNewStaticField(field);
                    else
                        batch.addNewField(field);
                }
            }
            if(prevIsChild != field.isChild()) {
                if(field.isChild())
                    batch.addToChildField(field);
                else
                    batch.addToNonChildField(field);
            }
            if (field.isEnumConstant() && (!prevName.equals(field.getName()) || prevOrdinal != field.getOrdinal()))
                batch.addModifiedEnumConstant(field);
            batch.getContext().updateMemoryIndex(field);
        }
        return field;
    }

    private Method readMethod(Klass declaringKlass) {
        var method = readEntity0(Method.class, declaringKlass);
        if(method.getParameters().isEmpty() && !method.isStatic() && method.getName().equals(Constants.RUN_METHOD_NAME))
            batch.addRunMethod(method);
        return method;
    }

    private Index readIndex(Klass declaringKlass) {
        var index = readEntity0(Index.class, declaringKlass);
        if (index.isNew())
            batch.addNewIndex(index);
        return index;
    }

    private static class KlassInfo {
        private int nextFieldTag;

        public KlassInfo(int nextFieldTag) {
            this.nextFieldTag = nextFieldTag;
        }

        int nextFieldTag() {
            return nextFieldTag++;
        }

    }

}
