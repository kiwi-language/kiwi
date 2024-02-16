package tech.metavm.util;

import tech.metavm.entity.ChildArray;
import tech.metavm.entity.Entity;

public class ChildMetaList<E extends Entity> implements MetaList<E> {

    ChildArray<E> array;

}
