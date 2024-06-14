package org.metavm.entity.generic.mocks;

import org.metavm.entity.EntityType;
import org.metavm.entity.TemplateVariable;

@EntityType(compiled = true)
public class GenericsFoo<T> extends GenericsBase<@TemplateVariable("Element") T> {
}
