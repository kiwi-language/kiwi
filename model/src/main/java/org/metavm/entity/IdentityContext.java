package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Instance;
import org.metavm.util.InternalException;
import org.metavm.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class IdentityContext {

    private static final Logger logger = LoggerFactory.getLogger(IdentityContext.class);

    private final Map<Entity, ModelIdentity> model2identity = new IdentityHashMap<>();
    private final Map<ModelIdentity, Entity> identity2model = new HashMap<>();

    public IdentityHashMap<Object, ModelIdentity> getIdentityMap(Entity object) {
        var buildKeyContext = new BuildKeyContext(this);
        getModelId(object, buildKeyContext, null);
        return buildKeyContext.getIdentityMap();
    }

    public String getModelName(Entity model, @NotNull BuildKeyContext buildKeyContext, @Nullable Object current) {
        return getModelId(model, buildKeyContext, current).name();
    }

    public ModelIdentity getModelId(Entity model) {
        return getModelId(model, new BuildKeyContext(this), null);
    }

    public ModelIdentity getModelId(Entity model, @NotNull BuildKeyContext buildKeyContext, @Nullable Object current) {
        var type = ReflectionUtils.getType(model);
        if (model == current) // Handle back reference from child to parent
            return new ModelIdentity(type, "this", true);
        var identity = model2identity.get(model);
        if (identity != null)
            return identity;
        if (model instanceof GlobalKey globalKey && globalKey.isValidGlobalKey()) {
            identity = new ModelIdentity(type, globalKey.getGlobalKey(buildKeyContext), false);
        } else {
            var parent = model.getParentEntity();
            if (parent != null) {
                var parentId = getModelId(parent, buildKeyContext, current);
                String fieldName;
                if (model instanceof LocalKey localKey && localKey.isValidLocalKey()) {
                    fieldName = localKey.getLocalKey(buildKeyContext);
                } else {
                    throw new IllegalStateException("Cannot build ID for entity: " + model + ", class: " + model.getClass().getName());
//                        logger.warn("Entity " + entity + " is in a list but does not have a key");
//                        fieldName = Integer.toString(((List<?>) parent).indexOf(entity));
                }
                identity = new ModelIdentity(type, parentId.name() + "." + fieldName, parentId.relative());
            } else
                throw new InternalException("Fail to create model identity for: " + EntityUtils.getEntityPath(model));
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

    public Instance getModel(ModelIdentity identity) {
        return identity2model.get(identity);
    }

    public Map<Entity, ModelIdentity> getIdentityMap() {
        return model2identity;
    }

}
