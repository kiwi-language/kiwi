package tech.metavm.object.instance.persistence;

import tech.metavm.entity.IndexOperator;

public record IndexQueryItemPO(String columnName, IndexOperator operator, byte[] value) {


}
