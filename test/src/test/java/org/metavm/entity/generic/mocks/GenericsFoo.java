package org.metavm.entity.generic.mocks;

import org.metavm.api.Entity;
import org.metavm.entity.TemplateVariable;

@Entity(compiled = true)
public class GenericsFoo<T> extends GenericsBase<@TemplateVariable("Element") T> {
}
