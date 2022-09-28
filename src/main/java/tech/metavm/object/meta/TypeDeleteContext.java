//package tech.metavm.object.meta;
//
//import tech.metavm.util.ContextUtil;
//import tech.metavm.util.BusinessException;
//import tech.metavm.util.NncUtils;
//
//import java.util.List;
//
//public class TypeDeleteContext {
//
//    public static void execute(long typeId, MetadataStore metadataStore) {
//        new TypeDeleteContext(ContextUtil.getTenantId(), typeId, metadataStore).execute();
//    }
//
//    private final long tenantId;
//    private final long typeId;
//    private final MetadataStore metadataStore;
//    private Type category;
//
//    public TypeDeleteContext(long tenantId, long modelId, MetadataStore metadataStore) {
//        this.tenantId = tenantId;
//        this.typeId = modelId;
//        this.metadataStore = metadataStore;
//    }
//
//    public final void execute() {
//        init();
//        validate();
//        delete();
//    }
//
//    private void init() {
//        category = metadataStore.getTypeFromDB(tenantId, typeId);
//    }
//
//    private void validate() {
//        if(category == null) {
//            throw BusinessException.invalidParams("对象ID不存在("+ typeId +")");
//        }
//        List<Field> nFields = metadataStore.getRelatingFields(tenantId, typeId, 3);
//        if(NncUtils.isNotEmpty(nFields)) {
//            if(nFields.size() > 1 || nFields.get(0).getOwner().getId() != typeId) {
//                String fieldNames = NncUtils.join(nFields, Field::getFullName, ", ");
//                throw BusinessException.deleteNClassError(category, "当前类型被其他类型属性关联(" + fieldNames + ")");
//            }
//        }
//    }
//
//    private void delete() {
//        metadataStore.deleteNClass(category);
//    }
//
//}
