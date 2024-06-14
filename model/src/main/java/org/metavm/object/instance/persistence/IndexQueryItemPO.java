package org.metavm.object.instance.persistence;

import org.metavm.entity.IndexOperator;

public record IndexQueryItemPO(String columnName, IndexOperator operator, byte[] value) {


}
