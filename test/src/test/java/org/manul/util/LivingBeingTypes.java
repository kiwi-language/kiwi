package org.manul.util;

import org.manul.object.type.ArrayType;
import org.manul.object.type.Field;
import org.manul.object.type.Klass;

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
