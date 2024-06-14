package org.metavm.util;

import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;

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
