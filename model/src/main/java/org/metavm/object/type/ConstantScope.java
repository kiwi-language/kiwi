package org.metavm.object.type;

import org.metavm.api.Entity;

import java.util.List;

@Entity
public interface ConstantScope {

    List<TypeVariable> getAllTypeParameters();

}
