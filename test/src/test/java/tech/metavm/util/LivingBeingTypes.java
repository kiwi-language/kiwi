package tech.metavm.util;

import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Field;

public record LivingBeingTypes(
        Klass livingBeingType,
        Klass animalType,
        Klass humanType,
        ArrayType livingBeingArrayType,
        Field livingBeingAgeField,
        Field livingBeingExtraInfoFIeld,
        Field livingBeingOffspringsField,
        Field livingBeingAncestorsField,
        Field animalIntelligenceField,
        Field humanOccupationField
) {
}
