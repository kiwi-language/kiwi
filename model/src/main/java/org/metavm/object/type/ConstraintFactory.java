package org.metavm.object.type;

import org.metavm.util.Utils;

import java.util.List;

public class ConstraintFactory {

    public static Index newUniqueConstraint(String name, List<Field> fields) {
        Utils.requireNotEmpty(fields, "fields can not empty");
        Klass type = fields.getFirst().getDeclaringType();
        String message = "Duplicate field '" + Utils.join(fields, Field::getQualifiedName) + "'";
        return new Index(type, name, message, true, fields, null);
    }

}
