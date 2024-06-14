package org.metavm.util;

import org.metavm.entity.ChildArray;
import org.metavm.entity.Entity;

public class ChildMetaList<E extends Entity> implements MetaList<E> {

    ChildArray<E> array;

}
