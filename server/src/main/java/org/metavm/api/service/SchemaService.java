package org.metavm.api.service;

import org.metavm.api.dto.*;
import org.metavm.common.ErrorCode;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.flow.Method;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.type.*;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.Utils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SchemaService extends EntityContextFactoryAware {

    public SchemaService(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    public SchemaResponse getSchema() {
        try (var context = entityContextFactory.newContext()) {
            context.loadKlasses();
            var refs = context.selectByKey(new IndexKeyRT(
                    Klass.IDX_ALL_FLAG.getIndex(),
                    List.of(Instances.trueInstance())
            ));
            var klasses = Utils.map(refs, ref -> (Klass) ref.resolveObject());
            var dtos = Utils.filterAndMap(klasses, k -> !k.isInner() && !k.isLocal(), this::buildClassDTO);
            return new SchemaResponse(dtos);
        }
    }

    private ClassDTO buildClassDTO(Klass klass) {
        var superTypes = new ArrayList<ClassTypeDTO>();
        if (klass.getSuperType() != null)
            superTypes.add(buildClassTypeDTO(klass.getSuperType()));
        klass.getInterfaces().forEach(t -> superTypes.add(buildClassTypeDTO(t)));
        var constructor = Utils.findRequired(klass.getMethods(), Method::isConstructor,
                () -> "Class " + klass.getQualifiedName() + " is missing constructor");
        return new ClassDTO(
                "public",
                lowerName(klass.getKind()),
                klass.isAbstract(),
                klass.getName(),
                klass.getQualifiedName(),
                superTypes,
                buildConstructorDTO(constructor),
                Utils.filterAndMap(klass.getFields(), f -> !f.isEnumConstant(), this::buildFieldDTO),
                Utils.filterAndMap(klass.getMethods(), m -> !m.isConstructor(), this::buildMethodDTO),
                Utils.map(klass.getKlasses(), this::buildClassDTO),
                Utils.map(klass.getEnumConstants(), this::buildEnumConstantDTO),
                klass.getAttribute(AttributeNames.BEAN_NAME),
                klass.getLabel()
        );
    }

    private ConstructorDTO buildConstructorDTO(Method method) {
        return new ConstructorDTO(Utils.map(method.getParameters(), this::buildParameterDTO));
    }

    private EnumConstantDTO buildEnumConstantDTO(Field field) {
        return new EnumConstantDTO(field.getName(), field.getLabel());
    }

    private MethodDTO buildMethodDTO(Method method) {
        return new MethodDTO(
                lowerName(method.getAccess()),
                method.isAbstract(),
                method.getName(),
                Utils.map(method.getParameters(), this::buildParameterDTO),
                buildTypeDTO(method.getReturnType()),
                method.getLabel()
        );
    }

    private TypeDTO buildTypeDTO(Type type) {
        return switch (type) {
            case AnyType ignored -> new PrimitiveTypeDTO("any");
            case NeverType ignored -> new PrimitiveTypeDTO("never");
            case NullType ignored -> new PrimitiveTypeDTO("null");
            case PrimitiveType primitiveType -> buildPrimitiveTypeDTO(primitiveType);
            case ClassType classType -> {
                if (classType.isString())
                    yield new PrimitiveTypeDTO("string");
                else
                    yield buildClassTypeDTO(classType);
            }
            case ArrayType arrayType -> buildArrayTypeDTO(arrayType);
            case UnionType unionType -> buildUnionTypeDTO(unionType);
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_SCHEMA,
                    "Type " + type.getClass().getSimpleName() + " is not yet supported");
        };
    }

    private UnionTypeDTO buildUnionTypeDTO(UnionType unionType) {
        return new UnionTypeDTO(Utils.map(unionType.getMembers(), this::buildTypeDTO));
    }

    private PrimitiveTypeDTO buildPrimitiveTypeDTO(PrimitiveType primitiveType) {
        return new PrimitiveTypeDTO(primitiveType.getName().toLowerCase());
    }

    private ArrayTypeDTO buildArrayTypeDTO(ArrayType arrayType) {
        return new ArrayTypeDTO(buildTypeDTO(arrayType.getElementType()));
    }

    private ClassTypeDTO buildClassTypeDTO(ClassType classType) {
        return new ClassTypeDTO(classType.getQualifiedName());
    }

    private ParameterDTO buildParameterDTO(Parameter param) {
        return new ParameterDTO(param.getName(), buildTypeDTO(param.getType()), param.getLabel());
    }

    private FieldDTO buildFieldDTO(Field field) {
        return new FieldDTO(
                lowerName(field.getAccess()),
                field.getName(),
                buildTypeDTO(field.getType()),
                field == field.getDeclaringType().getTitleField(),
                field.getLabel()
        );
    }

    private String lowerName(Enum<?> enum_) {
        return enum_.name().toLowerCase();
    }

}
