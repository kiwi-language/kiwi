package tech.metavm.util;

import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;

public record LivingBeingTypes(
        ClassType livingBeingType,
        ClassType animalType,
        ClassType humanType,
        ArrayType livingBeingArrayType,
        Field livingBeingAgeField,
        Field livingBeingExtraInfoFIeld,
        Field livingBeingOffspringsField,
        Field livingBeingAncestorsField,
        Field animalIntelligenceField,
        Field humanOccupationField
) {
}
