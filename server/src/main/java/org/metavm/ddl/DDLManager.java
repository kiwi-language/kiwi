package org.metavm.ddl;

import org.metavm.asm.AssemblerFactory;
import org.metavm.entity.*;
import org.metavm.flow.FlowSavingContext;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.SaveTypeBatch;
import org.metavm.object.type.TypeManager;
import org.metavm.object.type.rest.dto.FieldAdditionDTO;
import org.metavm.object.type.rest.dto.KlassDTO;
import org.metavm.object.type.rest.dto.PreUpgradeRequest;
import org.metavm.task.GlobalPreUpgradeTask;
import org.metavm.task.PreUpgradeTask;
import org.metavm.util.ChangeList;
import org.metavm.util.Constants;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.metavm.util.Constants.DDL_SESSION_TIMEOUT;

@Component
public class DDLManager extends EntityContextFactoryAware {

    private static final Logger logger = LoggerFactory.getLogger(DDLManager.class);

    private final TypeManager typeManager;

    public DDLManager(EntityContextFactory entityContextFactory, TypeManager typeManager) {
        super(entityContextFactory);
        this.typeManager = typeManager;
        GlobalPreUpgradeTask.preUpgradeAction = this::preUpgrade;
    }

    public PreUpgradeRequest buildUpgradePreparationRequest(int since) {
        var defContext = ModelDefRegistry.getDefContext();
        var klasses = NncUtils.exclude(defContext.getAllBufferedEntities(Klass.class), Entity::isEphemeralEntity);
        var fields = new ArrayList<FieldAdditionDTO>();
        var initializers = new ArrayList<KlassDTO>();
        var wal = new WAL(Constants.ROOT_APP_ID);
        var instancePOs = new ArrayList<InstancePO>();
        var newKlassIds = new ArrayList<String>();
        for (Klass klass : klasses) {
            if (klass.getSince() > since) {
                newKlassIds.add(klass.getStringId());
                var instance = defContext.getInstance(klass);
                instancePOs.add(instance.toPO(Constants.ROOT_APP_ID));
                var initializerKlass = tryBuildInitializerKlass(klass);
                if(initializerKlass != null)
                    initializers.add(initializerKlass);
            } else {
                boolean anyMatch = false;
                for (Field field : klass.getFields()) {
                    if (field.getSince() > since) {
                        fields.add(new FieldAdditionDTO(field.getStringId(), field.getName()));
                        anyMatch = true;
                    }
                }
                if (anyMatch) {
                    instancePOs.add(defContext.getInstance(klass).toPO(Constants.ROOT_APP_ID));
                    initializers.add(buildInitializerKlass(klass));
                }
            }
        }
        wal.saveInstances(ChangeList.inserts(instancePOs));
        wal.buildData();
        return new PreUpgradeRequest(fields, initializers, newKlassIds, wal.getData());
    }

    private KlassDTO buildInitializerKlass(Klass klass) {
        var klassDTO = tryBuildInitializerKlass(klass);
        if(klassDTO == null)
            throw new InternalException("Initializer assembly file is missing for class: " + klass.getCodeNotNull());
        return klassDTO;
    }

    private @Nullable KlassDTO tryBuildInitializerKlass(Klass klass) {
        var asmFile = "/initializers/" + klass.getCodeNotNull().replace('.', '/') + ".masm";
        try(var input = Klass.class.getResourceAsStream(asmFile)) {
            if(input == null)
                return null;
            var assembler = AssemblerFactory.createWithStandardTypes(ModelDefRegistry.getDefContext());
            return (KlassDTO) assembler.assemble(input).get(0);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read initializer source file for: " + klass.getName());
        }
    }

    @Transactional
    public void preUpgrade(PreUpgradeRequest request) {
        try(var platformContext = newPlatformContext()) {
            platformContext.bind(new GlobalPreUpgradeTask(request));
            platformContext.finish();
        }
    }

    private void preUpgrade(PreUpgradeRequest request, IEntityContext outerContext) {
        FlowSavingContext.initConfig();
        FlowSavingContext.skipPreprocessing(true);
        var defWAL = outerContext.bind(new WAL(Constants.ROOT_APP_ID, request.walContent()));
        var writeWAL = outerContext.bind(new WAL(outerContext.getAppId()));
        String ddlId;
        try(var defContext = DefContextUtils.createReversedDefContext(defWAL, entityContextFactory, request.newKlassIds())) {
            try (var context = entityContextFactory.newContext(outerContext.getAppId(), defContext,
                    builder -> builder
                            .timeout(DDL_SESSION_TIMEOUT)
                            .writeWAL(writeWAL)
            )) {
                var batch = typeManager.batchSave(request.initializerKlasses(), List.of(), context);
                var fieldAdds = new ArrayList<FieldAddition>();
                for (FieldAdditionDTO fieldAddDTO : request.fieldAdditions()) {
                    var field = context.getField(fieldAddDTO.fieldId());
                    fieldAdds.add(
                            new FieldAddition(
                                    field,
                                    getSystemFieldInitializer(batch, field.getDeclaringType(), fieldAddDTO.fieldName())
                            )
                    );
                }
                var runMethods = new ArrayList<Method>();
                for (KlassDTO k : request.initializerKlasses()) {
                    var initializer = context.getKlass(k.id());
                    var runMethod = initializer.findMethod(m -> m.isStatic() && m.getParameters().size() == 1 && m.getName().equals(Constants.RUN_METHOD_NAME));
                    if(runMethod != null)
                        runMethods.add(runMethod);
                }
                var ddl = new SystemDDL(fieldAdds, runMethods);
                context.bind(ddl);
                context.finish();
                ddlId = ddl.getStringId();
            }
        }
        outerContext.bind(new PreUpgradeTask(writeWAL, defWAL, request.newKlassIds(), ddlId));
        FlowSavingContext.clearConfig();
    }

    private Klass tryGetInitializerKlass(Klass klass, IEntityContext context) {
        return context.selectFirstByKey(Klass.UNIQUE_CODE, klass.getCodeNotNull() + "Initializer");
    }

    private Method getSystemFieldInitializer(SaveTypeBatch batch, Klass klass, String fieldName) {
        var initializerKlass = tryGetInitializerKlass(klass, batch.getContext());
        if(initializerKlass == null)
            throw new InternalException("Cannot find initializer class for " + klass.getName());
        var methodName = "__" + fieldName +"__";
        var method = initializerKlass.findMethod(m -> m.isStatic() && m.getName().equals(methodName) && m.getParameterTypes().equals(List.of(klass.getType())));
        if(method == null)
            throw new InternalException("Cannot find initializer for field " + klass.getCodeNotNull() + "." + fieldName);
        return method;
    }

}
