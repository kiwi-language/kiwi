package org.metavm.object.instance.rest.dto;

public sealed interface IntegerValueDTO extends NumberValueDTO permits ByteValueDTO, IntValueDTO, LongValueDTO, ShortValueDTO {
}
