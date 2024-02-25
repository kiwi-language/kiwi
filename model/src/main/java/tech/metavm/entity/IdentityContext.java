package tech.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ResolutionStage;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class IdentityContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityContext.class);

    private final Map<Object, ModelIdentity> model2identity = new IdentityHashMap<>();
    private final Map<ModelIdentity, Object> identity2model = new HashMap<>();

    public IdentityHashMap<Object, ModelIdentity> getIdentityMap(Object object) {
        var buildKeyContext = new BuildKeyContext(this);
        getModelId(object, buildKeyContext, null);
        return buildKeyContext.getIdentityMap();
    }

    public String getModelName(Object model, @NotNull BuildKeyContext buildKeyContext, @Nullable Object current) {
        return getModelId(model, buildKeyContext, current).name();
    }

    public ModelIdentity getModelId(Object model) {
        return getModelId(model, new BuildKeyContext(this), null);
    }

    public ModelIdentity getModelId(Object model, @NotNull BuildKeyContext buildKeyContext, @Nullable Object current) {
        var type = ReflectionUtils.getType(model);
        if (model == current) // Handle back reference from child to parent
            return new ModelIdentity(type, "this", true);
        var identity = model2identity.get(model);
        if (identity != null)
            return identity;
        switch (model) {
            case Enum<?> e -> identity = new ModelIdentity(type, type.getTypeName() + "." + e.name(), false);
            case GlobalKey globalKey when globalKey.isValidGlobalKey() ->
                    identity = new ModelIdentity(type, globalKey.getGlobalKey(buildKeyContext), false);
            case Entity entity -> {
                var parent = entity.getParentEntity();
                var parentField = entity.getParentEntityField();
                if (parent != null) {
                    var parentId = getModelId(parent, buildKeyContext, current);
                    String fieldName;
                    if (parentField != null)
                        fieldName = parentField.getName();
                    else if (entity instanceof LocalKey localKey && localKey.isValidLocalKey()) {
                        fieldName = localKey.getLocalKey(buildKeyContext);
                    } else {
                        LOGGER.warn("Entity " + entity + " is in a list but does not have a key");
                        fieldName = Integer.toString(((ChildArray<?>) parent).indexOf(entity));
                    }
                    identity = new ModelIdentity(type, parentId.name() + "." + fieldName, parentId.relative());
                } else
                    throw new InternalException("Fail to create model identity for: " + model);
            }
            default -> throw new InternalException("Fail to create model identity for: " + model);
        }
        if (!identity.relative() && !model2identity.containsKey(model)/* && isEntityReady(model)*/
                /*&& !pendingObjects.contains(EntityUtils.getRoot(model))*/) {
            model2identity.put(model, identity);
            identity2model.put(identity, model);
            buildKeyContext.addIdentity(model, identity);
//            if (model instanceof Entity entity)
//                EntityUtils.forEachReference(entity, r -> {
//                    if (!(r instanceof Instance) && !pendingObjects.contains(EntityUtils.getRoot(r)))
//                        getModelId(r, buildKeyContext, null);
//                });
        }
        return identity;
    }

    public Object getModel(ModelIdentity identity) {
        return identity2model.get(identity);
    }

    private boolean isEntityReady(Object entity) {
        var root = EntityUtils.getRoot(entity);
        if(root instanceof StagedEntity stagedEntity)
            return stagedEntity.getStage() == ResolutionStage.DEFINITION;
        else
            return true;
    }

    public Map<Object, ModelIdentity> getIdentityMap() {
        return model2identity;
    }

}
