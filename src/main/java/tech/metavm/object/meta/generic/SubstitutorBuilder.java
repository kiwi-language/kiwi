package tech.metavm.object.meta.generic;

import tech.metavm.object.meta.*;

import java.util.List;

public class SubstitutorBuilder {
    private final List<TypeVariable> typeParameters;
    private final List<? extends Type> typeArguments;
    private final GenericContext context;
    private ResolutionStage stage = ResolutionStage.DEFINITION;
    private TypeFactory typeFactory;
    private ClassType existing;
    private SaveTypeBatch batch;

    public static SubstitutorBuilder newBuilder(
            List<TypeVariable> typeParameters, List<? extends Type> typeArguments, GenericContext context) {
        return new SubstitutorBuilder(typeParameters, typeArguments, context);
    }

    public SubstitutorBuilder typeFactory(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        return this;
    }

    public SubstitutorBuilder batch(SaveTypeBatch batch) {
        this.batch = batch;
        return this;
    }

    public SubstitutorBuilder existing(ClassType existing) {
        this.existing = existing;
        return this;
    }

    public SubstitutorBuilder stage(ResolutionStage stage) {
        this.stage = stage;
        return this;
    }

    private SubstitutorBuilder(List<TypeVariable> typeParameters, List<? extends Type> typeArguments, GenericContext context) {
        this.typeParameters = typeParameters;
        this.typeArguments = typeArguments;
        this.context = context;
    }

    public Substitutor build() {
        return new Substitutor(
                context,
                typeParameters,
                typeArguments,
                typeFactory,
                stage,
                batch
        );
    }

}
