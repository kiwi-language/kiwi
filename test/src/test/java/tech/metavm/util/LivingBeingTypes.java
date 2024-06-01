package tech.metavm.util;

import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Klass;

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
