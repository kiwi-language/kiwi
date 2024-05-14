package tech.metavm.object.type.mocks;

import tech.metavm.entity.EntityRepository;
import tech.metavm.entity.MemTypeRegistry;
import tech.metavm.entity.mocks.MockEntityRepository;
import tech.metavm.object.type.*;
import tech.metavm.object.type.generic.SubstitutorV2;

import java.util.List;

public class TypeProviders {

    public final MemTypeRegistry typeRegistry = new MemTypeRegistry();
    public final EntityRepository entityRepository = new MockEntityRepository(typeRegistry);
    public final MockTypeDefRepository typeDefRepository = new MockTypeDefRepository();

    public SubstitutorV2 createSubstitutor(Object template, List<TypeVariable> typeParameters, List<? extends Type> typeArguments, ResolutionStage stage) {
        return new SubstitutorV2(
                template,
                typeParameters,
                typeArguments,
                stage
        );
    }

    public void addTypeDef(TypeDef typeDef) {
        entityRepository.bind(typeDef);
        typeDefRepository.save(typeDef);
    }

}
