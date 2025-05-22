package org.metavm.object.type;

import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.classfile.ClassFileReader;
import org.metavm.common.ErrorCode;
import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.flow.Flows;
import org.metavm.flow.KlassInput;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.persistence.SchemaManager;
import org.metavm.util.BusinessException;
import org.metavm.util.ContextUtil;
import org.metavm.util.DebugEnv;
import org.metavm.util.Instances;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.metavm.util.Constants.DDL_SESSION_TIMEOUT;

@Component
public class TypeManager extends EntityContextFactoryAware {

    public static final Logger logger = LoggerFactory.getLogger(TypeManager.class);

    private final BeanManager beanManager;

    private final SchemaManager schemaManager;

    public TypeManager(EntityContextFactory entityContextFactory, BeanManager beanManager, SchemaManager schemaManager) {
        super(entityContextFactory);
        this.beanManager = beanManager;
        this.schemaManager = schemaManager;
    }

    private void initClass(Klass klass, IInstanceContext context) {
        var classInit = klass.findMethodByName("__cinit__");
        if (classInit != null)
            Flows.execute(classInit.getRef(), null, List.of(), context);
    }

    @Transactional
    public String deploy(InputStream in) {
        schemaManager.createInstanceTable(ContextUtil.getAppId(), "instance_tmp");
        schemaManager.createIndexEntryTable(ContextUtil.getAppId(), "index_entry_tmp");
        SaveTypeBatch batch;
        try (var context = newContext(builder -> builder.timeout(DDL_SESSION_TIMEOUT))) {
            ContextUtil.setDDL(true);
            var wal = context.bind(new WAL(context.allocateRootId(), context.getAppId()));
            try (var bufferingContext = newContext(builder -> builder.timeout(DDL_SESSION_TIMEOUT).writeWAL(wal))) {
                batch = deploy(in, bufferingContext);
                bufferingContext.finish();
            }
            var commit = context.bind(batch.buildCommit(wal));
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
                if (DebugEnv.traceDeployment && klass.isRoot()) {
                    rebuildNodes(klass);
                    logger.trace("{}", klass.getText());
                }
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
                var sft = StaticFieldTable.getInstance(klass.getType(), context);
                var ec = sft.getEnumConstantByName(enumConstantName);
                return ec.getId();
            }
            else
                throw new BusinessException(ErrorCode.NOT_AN_ENUM_CLASS, klass.getName());
        }
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
