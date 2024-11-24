package org.metavm.entity;

public interface GenericElement {

    default Object getSelfOrCopySource() {
        var copySource = getCopySource();
        return copySource != null ? copySource : this;
    }

    default Object getRootCopySource() {
        var e = this;
        while (true) {
            var s = (GenericElement) e.getCopySource();
            if(s == null)
                return e;
            e = s;
        }
    }

    Object getCopySource();

    void setCopySource(Object copySource);

}
