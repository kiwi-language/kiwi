package org.metavm.object.type;

import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.common.ErrorCode;
import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.EntityQueryService;
import org.metavm.entity.IEntityContext;
import org.metavm.flow.DeployKlassInput;
import org.metavm.flow.FlowExecutionService;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.rest.TreeDTO;
import org.metavm.object.type.rest.dto.TreeResponse;
import org.metavm.object.type.rest.dto.TypeTreeQuery;
import org.metavm.object.version.VersionManager;
import org.metavm.object.version.Versions;
import org.metavm.task.TaskManager;
import org.metavm.util.BusinessException;
import org.metavm.util.ContextUtil;
import org.metavm.util.NncUtils;
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

    private final EntityQueryService entityQueryService;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final TaskManager taskManager;

    private FlowExecutionService flowExecutionService;

    private VersionManager versionManager;

    private final BeanManager beanManager;

    public TypeManager(EntityContextFactory entityContextFactory,
                       EntityQueryService entityQueryService,
                       TaskManager taskManager, BeanManager beanManager) {
        super(entityContextFactory);
        this.entityQueryService = entityQueryService;
        this.taskManager = taskManager;
        this.beanManager = beanManager;
    }

    public TreeResponse queryTrees(TypeTreeQuery query) {
        try (var context = newContext()) {
            List<?> entities;
            List<Long> removedIds;
            long version;
            if (query.version() == -1L) {
                entities = versionManager.getAllTypes(context);
                removedIds = List.of();
                version = Versions.getLatestVersion(context);
            } else {
                var patch = versionManager.pullInternal(query.version(), context);
                entities = NncUtils.merge(
                        NncUtils.map(patch.changedTypeDefIds(), context::getTypeDef),
                        NncUtils.map(patch.changedFunctionIds(), context::getFunction)
                );
                var removedInstanceIds = NncUtils.merge(
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
                    NncUtils.filterAndMap(entities,
                            t -> context.getInstance(t).isRoot(),
                            t -> getTypeTree(t, context)),
                    removedIds
            );
        }
    }

    private TreeDTO getTypeTree(Object entity, IEntityContext context) {
        var typeInstance = context.getInstance(entity);
        return typeInstance.toTree().toDTO();
    }

    private void initClass(Klass klass, IEntityContext context) {
        var classInit = klass.findMethodByName("__cinit__");
        if (classInit != null) {
            flowExecutionService.executeInternal(
                    classInit, null,
                    List.of(),
                    context
            );
        }
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
            var runningCommit = context.selectFirstByKey(Commit.IDX_RUNNING, true);
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
            batch.getNewEnumConstantDefs().forEach(ecd -> createEnumConstant(ecd, context));
            return batch;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readKlass(InputStream in, SaveTypeBatch batch) {
        var klassIn = new DeployKlassInput(in, batch);
        klassIn.readKlass();
    }

    public String getEnumConstantId(String klassName, String enumConstantName) {
        try(var context = newContext()) {
            var klass = context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, klassName);
            if(klass == null)
                throw new BusinessException(ErrorCode.CLASS_NOT_FOUND, klassName);
            if(klass.isEnum()) {
                var sft = StaticFieldTable.getInstance(klass, context);
                var ec = sft.getEnumConstantByName(enumConstantName);
                return ec.getStringId();
            }
            else
                throw new BusinessException(ErrorCode.NOT_AN_ENUM_CLASS, klass.getName());
        }
    }

    private void createEnumConstant(EnumConstantDef enumConstantDef, IEntityContext context) {
        var sft = StaticFieldTable.getInstance(enumConstantDef.getKlass(), context);
        var value = enumConstantDef.createEnumConstant(context.getInstanceContext());
        sft.set(enumConstantDef.getField(), value.getReference());
    }

    @Autowired
    public void setFlowExecutionService(FlowExecutionService flowExecutionService) {
        this.flowExecutionService = flowExecutionService;
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
                    }
                }
            }
            throw new BusinessException(ErrorCode.INVALID_ELEMENT_NAME, name);
        }
    }

}
