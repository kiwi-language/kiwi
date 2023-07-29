package tech.metavm.autograph;

import java.util.ArrayList;
import java.util.List;

public class InnerTypeQualifiedName extends AttributeQualifiedName {

    private final List<QualifiedName> typeArgumentNames;

    public InnerTypeQualifiedName(QualifiedName parent, String attributeName, List<QualifiedName> typeArgumentNames) {
        super(parent, attributeName);
        this.typeArgumentNames = new ArrayList<>(typeArgumentNames);
    }

    public List<QualifiedName> getTypeArgumentNames() {
        return typeArgumentNames;
    }
}
