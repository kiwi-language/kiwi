package org.manul.object.instance.persistence;

import org.manul.entity.IndexOperator;

public record IndexQueryItemPO(String columnName, IndexOperator operator, byte[] value) {


}
