package org.metavm.object.type;

import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.classfile.ClassFileReader;
import org.metavm.common.ErrorCode;
import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.ApplicationStatusAware;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.EntityContextFactory;
import org.metavm.flow.Flows;
import org.metavm.flow.KlassInput;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.persistence.SchemaManager;
import org.metavm.object.instance.search.InstanceSearchService;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.metavm.util.Constants.DDL_SESSION_TIMEOUT;

@Component
public class TypeManager extends ApplicationStatusAware implements DeployService {

    public static final Logger logger = LoggerFactory.getLogger(TypeManager.class);

    private final BeanManager beanManager;

    private final SchemaManager schemaManager;
    private final InstanceSearchService instanceSearchService;

    public TypeManager(EntityContextFactory entityContextFactory, BeanManager beanManager, SchemaManager schemaManager, InstanceSearchService instanceSearchService) {
        super(entityContextFactory);
        this.beanManager = beanManager;
        this.schemaManager = schemaManager;
        this.instanceSearchService = instanceSearchService;
        Commit.cleanupRemovingClassesHook = this::cleanupRemovingClasses;
    }

    private void initClass(Klass klass, IInstanceContext context) {
        var classInit = klass.findMethodByName("__cinit__");
        if (classInit != null)
            Flows.execute(classInit.getRef(), null, List.of(), context);
    }

    @Transactional
    public String deploy(long appId, InputStream in) {
        ContextUtil.setAppId(appId);
        return deploy(in);
    }

    @Override
    @Transactional
    public String deploy(InputStream in) {
        ensureApplicationActive();
        instanceSearchService.deleteTmpIndex(ContextUtil.getAppId());
        schemaManager.dropTmpTables(ContextUtil.getAppId());
        schemaManager.createInstanceTable(ContextUtil.getAppId(), "instance_tmp");
        schemaManager.createIndexEntryTable(ContextUtil.getAppId(), "index_entry_tmp");
        instanceSearchService.createIndex(ContextUtil.getAppId(), true);
        SaveTypeBatch batch;
        try (var context = newContext(builder -> builder.timeout(DDL_SESSION_TIMEOUT))) {
            ContextUtil.setDDL(true);
            try (var bufferingContext = newContext(builder -> builder.timeout(DDL_SESSION_TIMEOUT).migrating(true))) {
                batch = deploy(in, bufferingContext);
                bufferingContext.finish();
            }
            var commit = context.bind(batch.buildCommit());
            if(CommitState.MIGRATING.shouldSkip(commit))
                context.bind(CommitState.SUBMITTING.createTask(commit, context));
            else
                context.bind(CommitState.MIGRATING.createTask(commit, context));
            context.finish();
            return commit.getStringId();
        } finally {
            ContextUtil.setDDL(false);
        }
    }

    public SaveTypeBatch deploy(InputStream input, IInstanceContext context) {
        try (var zipIn = new ZipInputStream(input)) {
            var runningCommit = context.selectFirstByKey(Commit.IDX_RUNNING, Instances.trueInstance());
            if (runningCommit != null)
                throw new BusinessException(ErrorCode.COMMIT_RUNNING);
            var existingKlasses = context.loadKlasses();
            existingKlasses.forEach(Instance::setMarked);
            var batch = SaveTypeBatch.create(context);
            ZipEntry zipEntry;
            while ((zipEntry = zipIn.getNextEntry()) != null) {
                if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".mvclass"))
                    readKlass(zipIn, batch);
                zipIn.closeEntry();
            }
            var klasses = Types.sortKlassesByTopology(batch.getKlasses());
            if (DebugEnv.traceDeployment) {
                logger.trace("Deployed classes: {}", Utils.join(klasses, Klass::getQualifiedName));
            }
            for (Klass klass : klasses) {
                klass.clearMarked();
                klass.resetHierarchy();
                if (DebugEnv.traceDeployment && klass.isRoot()) {
                    rebuildNodes(klass);
                    logger.trace("{}", klass.getText());
                }
            }
            Instances.clearMarks(existingKlasses).forEach(k -> handleRemovedKlass(k, batch));
            for (Klass newClass : batch.getNewKlasses()) {
                if (!newClass.isInterface())
                    initClass(newClass, context);
            }
            batch.getNewStaticFields().forEach(idx -> idx.initialize(null, context));
            batch.applyDDLToEnumConstants();
            var beanDefReg = BeanDefinitionRegistry.getInstance(context);
            beanManager.createBeans(klasses, beanDefReg, context);
            return batch;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    protected void cleanupRemovingClasses(long appId) {
        try (var context = newContext(appId, b -> b.migrating(true))) {
            var klasses = context.loadKlasses();
            for (Klass klass : klasses) {
                if (klass.getState() == ClassTypeState.REMOVING) {
                    if (klass.isRoot())
                        context.remove(klass);
                    else
                        Objects.requireNonNull(klass.getScope()).removeKlass(klass);
                    var sft = StaticFieldTable.getInstance(klass.getType(), context);
                    context.remove(sft);
                }
            }
            context.finish();
        }
    }

    private void handleRemovedKlass(Klass klass, SaveTypeBatch batch) {
        var tracing = DebugEnv.traceDeployment;
        if (tracing)
            logger.trace("Removing klass {} ({})", klass.getQualifiedName(), klass.getId());
        klass.resetHierarchy();
        klass.setState(ClassTypeState.REMOVING);
        var scope = klass.getScope();
        if (scope != null)
            scope.addKlass(klass);
        var context = batch.getContext();
        if (klass.isBeanClass()) {
            if (tracing)
                logger.trace("Removing bean {}", klass.getAttribute(AttributeNames.BEAN_NAME));
            beanManager.removeBeans(List.of(klass), BeanDefinitionRegistry.getInstance(context), context);
        }
        StaticFieldTable.getInstance(klass.getType(), context).clear();
    }

    private void rebuildNodes(Klass clazz) {
        for (Method method : clazz.getMethods()) {
            method.rebuildNodes();
            for (Klass klass : method.getKlasses()) {
                rebuildNodes(klass);
            }
        }
        for (Klass klass : clazz.getKlasses()) {
            rebuildNodes(klass);
        }
    }
    private void readKlass(InputStream in, SaveTypeBatch batch) {
        var klassIn = new KlassInput(in, batch.getContext());
        var reader = new ClassFileReader(klassIn, batch.getContext(), batch);
        reader.read();
    }

    public Id getEnumConstantId(String klassName, String enumConstantName) {
        try(var context = newContext()) {
            var klass = context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance(klassName));
            if(klass == null)
                throw new BusinessException(ErrorCode.CLASS_NOT_FOUND, klassName);
            if(klass.isEnum()) {
                klass.resetHierarchy();
                klass.getKlasses().forEach(Klass::resetHierarchy);
                var sft = StaticFieldTable.getInstance(klass.getType(), context);
                var ec = sft.getEnumConstantByName(enumConstantName);
                return ec.getId();
            }
            else
                throw new BusinessException(ErrorCode.NOT_AN_ENUM_CLASS, klass.getName());
        }
    }

    public Integer getSourceTag(long appId, String name) {
        ensureAppAccess(appId);
        try (var context = newContext(appId)) {
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
