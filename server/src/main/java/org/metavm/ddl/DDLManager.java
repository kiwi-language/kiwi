package org.metavm.ddl;

import org.metavm.asm.AssemblerFactory;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.flow.FlowSavingContext;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.SaveTypeBatch;
import org.metavm.object.type.TypeManager;
import org.metavm.object.type.rest.dto.FieldAdditionDTO;
import org.metavm.object.type.rest.dto.KlassDTO;
import org.metavm.object.type.rest.dto.PreUpgradeRequest;
import org.metavm.task.PreUpgradeTask;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.metavm.util.Constants.DDL_SESSION_TIMEOUT;

@Component
public class DDLManager extends EntityContextFactoryAware {

    private final TypeManager typeManager;

    public DDLManager(EntityContextFactory entityContextFactory, TypeManager typeManager) {
        super(entityContextFactory);
        this.typeManager = typeManager;
    }

    public PreUpgradeRequest buildUpgradePreparationRequest(int since) {
        var defContext = ModelDefRegistry.getDefContext();
        var klasses = NncUtils.exclude(defContext.getAllBufferedEntities(Klass.class), Entity::isEphemeralEntity);
        var fields = new ArrayList<FieldAdditionDTO>();
        var initializers = new ArrayList<KlassDTO>();
        for (Klass klass : klasses) {
            boolean anyMatch = false;
            for (Field field : klass.getFields()) {
                if(field.getSince() > since) {
                    fields.add(new FieldAdditionDTO(klass.getStringId(), field.getName(), field.getTag()));
                    anyMatch = true;
                }
            }
            if(anyMatch)
                initializers.add(buildInitializerKlass(klass));
        }
        return new PreUpgradeRequest(fields, initializers);
    }

    private KlassDTO buildInitializerKlass(Klass klass) {
        var asmFile = "/initializers/" + klass.getCodeNotNull().replace('.', '/') + ".masm";
        try(var input = Klass.class.getResourceAsStream(asmFile)) {
            if(input == null)
                throw new InternalException("Initializer assembly file is missing for class: " + klass.getCodeNotNull());
            var assembler = AssemblerFactory.createWithStandardTypes(ModelDefRegistry.getDefContext());
            return (KlassDTO) assembler.assemble(input).get(0);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read initializer source file for: " + klass.getName());
        }
    }

    @Transactional
    public void preUpgrade(PreUpgradeRequest request) {
        FlowSavingContext.initConfig();
        FlowSavingContext.skipPreprocessing(true);
        try(var context = newContext()) {
            var wal = context.bind(new WAL(context.getAppId()));
            String ddlId;
            try (var bufferingContext = newContext(builder -> builder.timeout(DDL_SESSION_TIMEOUT).writeWAL(wal))) {
                var batch = typeManager.batchSave(request.initializerKlasses(), List.of(), bufferingContext);
                var fieldAdds = new ArrayList<FieldAddition>();
                for (FieldAdditionDTO fieldAddDTO : request.newSystemFields()) {
                    var klass = bufferingContext.getKlass(fieldAddDTO.klassId());
                    if (klass != null) {
                        fieldAdds.add(
                                new FieldAddition(
                                        klass,
                                        fieldAddDTO.fieldTag(),
                                        getSystemFieldInitializer(batch, klass, fieldAddDTO.fieldName())
                                )
                        );
                    }
                }
                var ddl = new SystemDDL(fieldAdds);
                bufferingContext.bind(ddl);
                bufferingContext.finish();
                ddlId = ddl.getStringId();
            }
            context.bind(new PreUpgradeTask(wal, ddlId));
            context.finish();
        }
        finally {
            FlowSavingContext.clearConfig();
        }
    }

    private Method getSystemFieldInitializer(SaveTypeBatch batch, Klass klass, String fieldName) {
        var initializerKlass = batch.getContext().selectFirstByKey(Klass.UNIQUE_CODE, klass.getCodeNotNull() + "Initializer");
        if(initializerKlass == null)
            throw new InternalException("Cannot find initializer class for " + klass.getName());
        var methodName = "__" + fieldName +"__";
        var method = initializerKlass.findMethod(m -> m.isStatic() && m.getName().equals(methodName) && m.getParameterTypes().equals(List.of(klass.getType())));
        if(method == null)
            throw new InternalException("Cannot find initializer for field " + klass.getCodeNotNull() + "." + fieldName);
        return method;
    }

}
