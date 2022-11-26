package tech.metavm.object.meta;

import tech.metavm.entity.Model;

import java.util.List;

public interface IType extends Model {

    String getName();

    TypeCategory getCategory();

    List<Field> getFields();

}
