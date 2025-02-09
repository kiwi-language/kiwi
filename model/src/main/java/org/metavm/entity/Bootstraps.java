package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.type.*;
import org.metavm.util.*;

import java.lang.reflect.Field;
import java.util.Set;

import static org.metavm.util.Constants.ROOT_APP_ID;

@Slf4j
public class Bootstraps {

    public static BootstrapResult boot(
            StdAllocators stdAllocators,
            ColumnStore columnStore,
            TypeTagStore typeTagStore,
            Set<Class<?>> classBlackList,
            Set<Field> fieldBlacklist,
            boolean forceReboot
    ) {
        if (!forceReboot && ModelDefRegistry.isDefContextPresent())
            return new BootstrapResult(0, ModelDefRegistry.getDefContext());
        try (var ignoredEntry = ContextUtil.getProfiler().enter("Bootstrap.boot")) {
            ThreadConfigs.sharedParameterizedElements(true);
            ContextUtil.setAppId(ROOT_APP_ID);
            var identityContext = new IdentityContext();
            var defContext = new SystemDefContext(stdAllocators, typeTagStore, identityContext);
            defContext.setFieldBlacklist(fieldBlacklist);
            ModelDefRegistry.setDefContext(defContext);
            var entityClasses = EntityUtils.getModelClasses();
            for (ResolutionStage stage : ResolutionStage.values()) {
                for (Class<?> entityClass : entityClasses) {
                    if (!entityClass.isAnonymousClass() && !classBlackList.contains(entityClass))
                        defContext.getDef(entityClass);
                }
            }
            defContext.postProcess();
            defContext.flush();
            ModelDefRegistry.setDefContext(defContext);
            var idNullInstances = Utils.filter(defContext.entities(), inst -> inst.isDurable() && !inst.isValue() && inst.tryGetTreeId() == null);
            if (!idNullInstances.isEmpty()) {
                log.warn(idNullInstances.size() + " instances have null ids. Save is required");
                if (DebugEnv.bootstrapVerbose) {
                    for (int i = 0; i < Math.min(10, idNullInstances.size()); i++) {
                        var inst = idNullInstances.get(i);
                        log.warn("instance with null id: {}, identity: {}", Instances.getInstancePath(inst),
                                identityContext.getModelId((Entity) inst));
                    }
                }
            }
            ContextUtil.clearContextInfo();
            return new BootstrapResult(idNullInstances.size(), defContext);
        } finally {
            ThreadConfigs.sharedParameterizedElements(false);
        }
    }

}
