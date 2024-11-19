package org.metavm.object.type.mocks;

import org.metavm.entity.EntityRepository;
import org.metavm.entity.MemTypeRegistry;
import org.metavm.entity.mocks.MockEntityRepository;
import org.metavm.object.type.ResolutionStage;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeDef;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.generic.SubstitutorV2;

import java.util.List;

public class TypeProviders {

    public final MemTypeRegistry typeRegistry = new MemTypeRegistry();
    public final EntityRepository entityRepository = new MockEntityRepository();
    public final MockTypeDefRepository typeDefRepository = new MockTypeDefRepository();

    public SubstitutorV2 createSubstitutor(Object template, List<TypeVariable> typeParameters, List<? extends Type> typeArguments, ResolutionStage stage) {
        return new SubstitutorV2(
                template,
                typeParameters,
                typeArguments,
                null, stage
        );
    }

    public void addTypeDef(TypeDef typeDef) {
        entityRepository.bind(typeDef);
        typeDefRepository.save(typeDef);
    }

}
