package org.metavm.object.type;

import org.metavm.util.NncUtils;

import java.util.List;

public class ConstraintFactory {

    public static Index newUniqueConstraint(String name, List<Field> fields) {
        NncUtils.requireNotEmpty(fields, "fields can not empty");
        Klass type = fields.get(0).getDeclaringType();
        String message = "Duplicate field '" + NncUtils.join(fields, Field::getQualifiedName) + "'";
        return new Index(type, name, message, true, fields, null);
    }

}
