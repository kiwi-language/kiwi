package tech.metavm.entity.generic.mocks;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.TemplateVariable;

@EntityType(compiled = true)
public class GenericsFoo<T> extends GenericsBase<@TemplateVariable("Element") T> {
}
