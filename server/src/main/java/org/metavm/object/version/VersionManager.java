package org.metavm.object.version;

import org.metavm.entity.*;
import org.metavm.flow.Function;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeDef;
import org.metavm.util.Instances;
import org.metavm.util.Utils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VersionManager extends EntityContextFactoryAware {

    public VersionManager(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    public InternalMetaPatch pullInternal(long baseVersion, IEntityContext context) {
        List<Version> versions = context.query(Version.IDX_VERSION.newQueryBuilder()
                .from(new EntityIndexKey(List.of(Instances.longInstance(baseVersion + 1))))
                .limit(100)
                .build()
        );
        if (versions.isEmpty()) {
            return new InternalMetaPatch(baseVersion, baseVersion,
                    List.of(), List.of(), List.of(), List.of());
        }
        var typeIds = Utils.flatMapUnique(versions, Version::getChangedTypeIds);
        var removedTypeIds = Utils.flatMapUnique(versions, Version::getRemovedTypeIds);
        var functionIds = Utils.flatMapUnique(versions, Version::getChangedFunctionIds);
        var removedFunctionIds = Utils.flatMapUnique(versions, Version::getRemovedFunctionIds);

        typeIds = Utils.diffSet(typeIds, removedTypeIds);
        functionIds = Utils.diffSet(functionIds, removedFunctionIds);

        return new InternalMetaPatch(baseVersion, versions.getLast().getVersion(),
                new ArrayList<>(typeIds),
                new ArrayList<>(removedTypeIds),
                new ArrayList<>(functionIds),
                new ArrayList<>(removedFunctionIds)
        );
    }

    public List<Klass> getAllKlasses(IEntityContext context) {
        var defContext = context.getDefContext();
        //                Utils.exclude(defContext.getAllBufferedEntities(TypeDef.class), Entity::isEphemeral)
        //                Utils.exclude(defContext.getAllBufferedEntities(TypeDef.class), Entity::isEphemeral)
        return new ArrayList<>(context.selectByKey(Klass.IDX_ALL_FLAG, Instances.trueInstance()));
    }

    private List<Function> getAllFunctions(IEntityContext context) {
        var defContext = context.getDefContext();
        var functions = new ArrayList<>(defContext.getAllBufferedEntities(Function.class));
        functions.addAll(context.selectByKey(Function.IDX_ALL_FLAG, Instances.trueInstance()));
        return functions;
    }


}
