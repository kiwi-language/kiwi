package org.manul.object.type;

import org.manul.api.Entity;

import java.util.List;

@Entity
public interface ConstantScope {

    List<TypeVariable> getAllTypeParameters();

}
