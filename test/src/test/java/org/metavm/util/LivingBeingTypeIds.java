package org.metavm.util;

public record LivingBeingTypeIds(
        String livingBeingTypeId,
        String animalTypeId,
        String humanTypeId,
        String sentientTypeId,
        String livingBeingAgeFieldId,
        String livingBeingExtraFieldId,
        String livingBeingOffspringsFieldId,
        String livingBeingAncestorsFieldId,
        String animalIntelligenceFieldId,
        String humanOccupationFieldId,
        String humanThinkingFieldId,
        String livingBeingConstructorId,
        String animalConstructorId,
        String humanConstructorId,
        String makeSoundMethodId,
        String thinkMethodId
) {
}
