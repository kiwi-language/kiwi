package tech.metavm.object.meta;

import tech.metavm.object.meta.persistence.FieldPO;

public record FieldInfo(
        long id,
        long typeId,
        String name,
        boolean unique,
        boolean asTitle,
        String columnName
) {

    static FieldInfo createReference(long id, long typeId, String name, String columnName) {
        return new FieldInfo(
                id,
                typeId,
                name,
                false,
                false,
                columnName
        );
    }

    static FieldInfo createString(long id, String name, String columnName) {
        return new FieldInfo(
                id,
                StdTypeConstants.STRING,
                name,
                false,
                false,
                columnName
        );
    }

    static FieldInfo createPassword(long id, String name, String columnName) {
        return new FieldInfo(
                id,
                StdTypeConstants.PASSWORD,
                name,
                false,
                false,
                columnName
        );
    }

    static FieldInfo createTitle(long id, String name, String columnName) {
        return new FieldInfo(
                id,
                StdTypeConstants.STRING,
                name,
                false,
                true,
                columnName
        );
    }


    static FieldInfo createUniqueString(long id, String name, String columnName) {
        return new FieldInfo(
                id,
                StdTypeConstants.STRING,
                name,
                true,
                false,
                columnName
        );
    }
    
    public FieldPO toPO(long declaringTypeId) {
        FieldPO po = new FieldPO();
        po.setId(id);
        po.setName(name);
        po.setTenantId(-1L);
        po.setDeclaringTypeId(declaringTypeId);
        po.setTypeId(typeId);
        po.setAccess(Access.GLOBAL.code());
        po.setColumnName(columnName);
        po.setUnique(unique);
        po.setAsTitle(asTitle);
        return po;
    }

}
