package org.metavm.entity.generic.mocks;

import org.metavm.api.EntityType;
import org.metavm.entity.TemplateVariable;

@EntityType(compiled = true)
public class GenericsFoo<T> extends GenericsBase<@TemplateVariable("Element") T> {
}
