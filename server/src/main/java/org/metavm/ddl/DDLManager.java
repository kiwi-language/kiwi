//package org.metavm.ddl;
//
//import org.metavm.compiler.AssemblerFactory;
//import org.metavm.entity.*;
//import org.metavm.flow.FlowSavingContext;
//import org.metavm.flow.KlassOutput;
//import org.metavm.flow.Method;
//import org.metavm.object.instance.core.Id;
//import org.metavm.object.instance.core.WAL;
//import org.metavm.object.instance.persistence.InstancePO;
//import org.metavm.object.type.Field;
//import org.metavm.object.type.Klass;
//import org.metavm.object.type.SaveTypeBatch;
//import org.metavm.object.type.TypeManager;
//import org.metavm.object.type.rest.dto.FieldAdditionDTO;
//import org.metavm.object.type.rest.dto.PreUpgradeRequest;
//import org.metavm.task.GlobalPreUpgradeTask;
//import org.metavm.task.PreUpgradeTask;
//import org.metavm.util.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.annotation.Nullable;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
//
//import static org.metavm.util.Constants.DDL_SESSION_TIMEOUT;
//
//@Component
//public class DDLManager extends EntityContextFactoryAware {
//
//    private static final Logger logger = LoggerFactory.getLogger(DDLManager.class);
//
//    private final TypeManager typeManager;
//
//    public DDLManager(EntityContextFactory entityContextFactory, TypeManager typeManager) {
//        super(entityContextFactory);
//        this.typeManager = typeManager;
//        GlobalPreUpgradeTask.preUpgradeAction = this::preUpgrade;
//    }
//
//    public PreUpgradeRequest buildUpgradePreparationRequest(int since) {
//        var defContext = ModelDefRegistry.getDefContext();
//        var klasses = NncUtils.exclude(defContext.getAllBufferedEntities(Klass.class), Entity::isEphemeralEntity);
//        var fields = new ArrayList<FieldAdditionDTO>();
//        var initializers = new ArrayList<Klass>();
//        var wal = new WAL(Constants.ROOT_APP_ID);
//        var instancePOs = new ArrayList<InstancePO>();
//        var newKlassIds = new ArrayList<String>();
//        for (Klass klass : klasses) {
//            if (klass.getSince() > since) {
//                newKlassIds.add(klass.getStringId());
//                instancePOs.add(Instances.toPO(klass, Constants.ROOT_APP_ID));
//                var initializerKlass = tryBuildInitializerKlass(klass);
//                if(initializerKlass != null)
//                    initializers.add(initializerKlass);
//            } else {
//                boolean anyMatch = false;
//                for (Field field : klass.getFields()) {
//                    if (field.getSince() > since) {
//                        fields.add(new FieldAdditionDTO(field.getStringId(), field.getName()));
//                        anyMatch = true;
//                    }
//                }
//                if (anyMatch) {
//                    instancePOs.add(Instances.toPO(klass, Constants.ROOT_APP_ID));
//                    initializers.add(buildInitializerKlass(klass));
//                }
//            }
//        }
//        wal.saveInstances(ChangeList.inserts(instancePOs));
//        wal.buildData();
//        return new PreUpgradeRequest(fields, writeKlasses(initializers), newKlassIds, wal.getData());
//    }
//
//    private String writeKlasses(List<Klass> klasses) {
//        var bout = new ByteArrayOutputStream();
//        try(var serContext = SerializeContext.enter(); var zipOut = new ZipOutputStream(bout)) {
//            for (Klass klass : klasses) {
//                zipOut.putNextEntry(new ZipEntry(klass.getClassFilePath()));
//                klass.writeTo(new KlassOutput(zipOut, serContext));
//                zipOut.closeEntry();
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return EncodingUtils.encodeBase64(bout.toByteArray());
//    }
//
//    private Klass buildInitializerKlass(Klass klass) {
//        var k = tryBuildInitializerKlass(klass);
//        if(k == null)
//            throw new InternalException("Initializer assembly file is missing for class: " + klass.getQualifiedName());
//        return k;
//    }
//
//    private @Nullable Klass tryBuildInitializerKlass(Klass klass) {
//        var asmFile = "/initializers/" + Objects.requireNonNull(klass.getQualifiedName()).replace('.', '/') + ".kiwi";
//        try(var input = Klass.class.getResourceAsStream(asmFile)) {
//            if(input == null)
//                return null;
//            var assembler = AssemblerFactory.createWithStandardTypes(ModelDefRegistry.getDefContext());
//            assembler.assemble(input);
//            return assembler.getGeneratedKlasses().getFirst();
//        }
//        catch (IOException e) {
//            throw new RuntimeException("Failed to read initializer source file for: " + klass.getName());
//        }
//    }
//
//    @Transactional
//    public void preUpgrade(PreUpgradeRequest request) {
//        try(var platformContext = newPlatformContext()) {
//            var defWal = new WAL(Constants.ROOT_APP_ID, request.walContent());
//            platformContext.bind(new GlobalPreUpgradeTask(request, defWal));
//            platformContext.finish();
//        }
//    }
//
//    private void preUpgrade(PreUpgradeRequest request, WAL defWAL, IInstanceContext outerContext) {
//        FlowSavingContext.initConfig();
//        FlowSavingContext.skipPreprocessing(true);
//        var writeWAL = outerContext.bind(new WAL(outerContext.getAppId()));
//        Id ddlId;
//        var defContext = DefContextUtils.createReversedDefContext(defWAL, entityContextFactory, request.newKlassIds());
//        try (var context = entityContextFactory.newContext(outerContext.getAppId(), defContext,
//                builder -> builder
//                        .timeout(DDL_SESSION_TIMEOUT)
//                        .writeWAL(writeWAL)
//        )) {
//            var batch = typeManager.deploy(createInputStreamFromBase64(request.initializers()), context);
//            var initializers = new ArrayList<>(batch.getKlasses());
//            var fieldAdds = new ArrayList<FieldAddition>();
//            for (FieldAdditionDTO fieldAddDTO : request.fieldAdditions()) {
//                var field = context.getField(fieldAddDTO.fieldId());
//                fieldAdds.add(
//                        new FieldAddition(
//                                field,
//                                getSystemFieldInitializer(batch, field.getDeclaringType(), fieldAddDTO.fieldName())
//                        )
//                );
//            }
//            var runMethods = new ArrayList<Method>();
//            for (var initializer : initializers) {
//                var runMethod = initializer.findMethod(m -> m.isStatic() && m.getParameters().size() == 1 && m.getName().equals(Constants.RUN_METHOD_NAME));
//                if(runMethod != null)
//                    runMethods.add(runMethod);
//            }
//            var ddl = new SystemDDL(fieldAdds, runMethods);
//            context.bind(ddl);
//            context.finish();
//            ddlId = ddl.getId();
//        }
//        finally {
//            SystemConfig.clearLocal();
//        }
//        outerContext.bind(new PreUpgradeTask(writeWAL, defWAL.getId(), NncUtils.map(request.newKlassIds(), Id::parse), ddlId));
//        FlowSavingContext.clearConfig();
//    }
//
//    private InputStream createInputStreamFromBase64(String base64) {
//        var bytes = EncodingUtils.decodeBase64(base64);
//        return new ByteArrayInputStream(bytes);
//    }
//
//    private Klass tryGetInitializerKlass(Klass klass, IInstanceContext context) {
//        return context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME,
//                Instances.stringInstance(klass.getQualifiedName() + "Initializer"));
//    }
//
//    private Method getSystemFieldInitializer(SaveTypeBatch batch, Klass klass, String fieldName) {
//        var initializerKlass = tryGetInitializerKlass(klass, batch.getContext());
//        if(initializerKlass == null)
//            throw new InternalException("Cannot find initializer class for " + klass.getName());
//        var methodName = "__" + fieldName +"__";
//        var method = initializerKlass.findMethod(m -> m.isStatic() && m.getName().equals(methodName) && m.getParameterTypes().equals(List.of(klass.getType())));
//        if(method == null)
//            throw new InternalException("Cannot find initializer for field " + klass.getQualifiedName() + "." + fieldName);
//        return method;
//    }
//
//}
