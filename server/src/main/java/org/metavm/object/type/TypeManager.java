package org.metavm.object.type;

import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.common.ErrorCode;
import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.IEntityContext;
import org.metavm.flow.DeployKlassInput;
import org.metavm.flow.Flows;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.rest.TreeDTO;
import org.metavm.object.type.rest.dto.TreeResponse;
import org.metavm.object.type.rest.dto.TypeTreeQuery;
import org.metavm.object.version.VersionManager;
import org.metavm.object.version.Versions;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.metavm.util.Constants.DDL_SESSION_TIMEOUT;

@Component
public class TypeManager extends EntityContextFactoryAware {

    public static final Logger logger = LoggerFactory.getLogger(TypeManager.class);

    private VersionManager versionManager;

    private final BeanManager beanManager;

    public TypeManager(EntityContextFactory entityContextFactory, BeanManager beanManager) {
        super(entityContextFactory);
        this.beanManager = beanManager;
    }

    public TreeResponse queryTrees(TypeTreeQuery query) {
        try (var context = newContext()) {
            List<? extends Instance> entities;
            List<Long> removedIds;
            long version;
            if (query.version() == -1L) {
                entities = versionManager.getAllKlasses(context);
                removedIds = List.of();
                version = Versions.getLatestVersion(context);
            } else {
                var patch = versionManager.pullInternal(query.version(), context);
                entities = Utils.merge(
                        Utils.map(patch.changedTypeDefIds(), context::getTypeDef),
                        Utils.map(patch.changedFunctionIds(), context::getFunction)
                );
                var removedInstanceIds = Utils.merge(
                        patch.removedTypeDefIds(),
                        patch.removedFunctionIds()
                );
                removedIds = new ArrayList<>();
                for (String removedInstanceId : removedInstanceIds) {
                    var id = Id.parse(removedInstanceId);
                    if (id instanceof PhysicalId physicalId && physicalId.getNodeId() == 0L)
                        removedIds.add(id.getTreeId());
                }
                version = patch.version();
            }
            return new TreeResponse(
                    version,
                    Utils.filterAndMap(entities,
                            Instance::isRoot,
                            t -> getTypeTree(t, context)),
                    removedIds
            );
        }
    }

    private TreeDTO getTypeTree(Instance entity, IEntityContext context) {
        return entity.toTree().toDTO();
    }

    private void initClass(Klass klass, IEntityContext context) {
        var classInit = klass.findMethodByName("__cinit__");
        if (classInit != null)
            Flows.execute(classInit.getRef(), null, List.of(), context);
    }

    @Transactional
    public String deploy(InputStream in) {
        SaveTypeBatch batch;
        try (var context = newContext(builder -> builder.timeout(DDL_SESSION_TIMEOUT))) {
            ContextUtil.setDDL(true);
            var wal = context.bind(new WAL(context.getAppId()));
            try (var bufferingContext = newContext(builder -> builder.timeout(DDL_SESSION_TIMEOUT).writeWAL(wal))) {
                batch = deploy(in, bufferingContext);
                bufferingContext.finish();
            }
            var commit = context.bind(batch.buildCommit(wal));
            if(CommitState.PREPARING0.shouldSkip(commit))
                context.bind(CommitState.SUBMITTING.createTask(commit));
            else
                context.bind(CommitState.PREPARING0.createTask(commit));
            context.finish();
            return commit.getStringId();
        } finally {
            ContextUtil.setDDL(false);
        }
    }

    public SaveTypeBatch deploy(InputStream input, IEntityContext context) {
        try (var zipIn = new ZipInputStream(input)) {
            var runningCommit = context.selectFirstByKey(Commit.IDX_RUNNING, Instances.trueInstance());
            if (runningCommit != null)
                throw new BusinessException(ErrorCode.COMMIT_RUNNING);
            var batch = SaveTypeBatch.create(context);
            ZipEntry zipEntry;
            while ((zipEntry = zipIn.getNextEntry()) != null) {
                if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".mvclass"))
                    readKlass(zipIn, batch);
                zipIn.closeEntry();
            }
            var klasses = Types.sortKlassesByTopology(batch.getKlasses());
            for (Klass klass : klasses) {
                klass.resetHierarchy();
            }
            beanManager.createBeans(klasses, BeanDefinitionRegistry.getInstance(context), context);
            for (Klass newClass : batch.getNewKlasses()) {
                if (!newClass.isInterface())
                    initClass(newClass, context);
            }
            batch.getNewStaticFields().forEach(idx -> idx.initialize(null, context));
            batch.applyDDLToEnumConstants();
            return batch;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readKlass(InputStream in, SaveTypeBatch batch) {
        var klassIn = new DeployKlassInput(in, batch);
        klassIn.readEntity(Klass.class, null);
    }

    public String getEnumConstantId(String klassName, String enumConstantName) {
        try(var context = newContext()) {
            var klass = context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance(klassName));
            if(klass == null)
                throw new BusinessException(ErrorCode.CLASS_NOT_FOUND, klassName);
            if(klass.isEnum()) {
                klass.resetHierarchy();
                var sft = StaticFieldTable.getInstance(klass.getType(), context);
                var ec = sft.getEnumConstantByName(enumConstantName);
                return ec.getStringId();
            }
            else
                throw new BusinessException(ErrorCode.NOT_AN_ENUM_CLASS, klass.getName());
        }
    }

    @Autowired
    public void setVersionManager(VersionManager versionManager) {
        this.versionManager = versionManager;
    }

    public Integer getSourceTag(String name) {
        try (var context = newContext()) {
            var klass = context.findKlassByQualifiedName(name);
            if(klass != null)
                return klass.getSourceTag();
            else {
                var idx = name.lastIndexOf('.');
                if(idx > 0 && idx < name.length() - 1) {
                    var klassName = name.substring(0, idx);
                    var fieldName = name.substring(idx + 1);
                    klass = context.findKlassByQualifiedName(klassName);
                    if(klass != null) {
                        var field = klass.findFieldByName(fieldName);
                        if(field != null)
                            return field.getSourceTag();
                        var staticField = klass.findStaticFieldByName(fieldName);
                        if (staticField != null)
                            return staticField.getSourceTag();
                    }
                }
            }
            throw new BusinessException(ErrorCode.INVALID_ELEMENT_NAME, name);
        }
    }

    public Id getKlassId(String qualifiedName) {
        try (var context = entityContextFactory.newContext()) {
            return context.getKlassByQualifiedName(qualifiedName).getId();
        }
    }

}
