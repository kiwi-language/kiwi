package tech.metavm.object.meta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.persistence.mappers.TypeMapper;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PrimitiveTypeInitializer {

    @Autowired
    private TypeMapper typeMapper;

    private static record TypeInfo(
            long id,
            String name,
            TypeCategory category
    ) {
    }

    public static final TypeInfo[] TYPE_INFO_LIST = new TypeInfo[] {
//            new TypeInfo(1, "对象", TypeCategory.OBJECT),
            new TypeInfo(4, "INT32", TypeCategory.INT),
            new TypeInfo(5, "整数", TypeCategory.LONG),
            new TypeInfo(7, "数值", TypeCategory.DOUBLE),
            new TypeInfo(8, "是否", TypeCategory.BOOL),
            new TypeInfo(9, "文本", TypeCategory.STRING),
            new TypeInfo(10, "时间", TypeCategory.TIME),
            new TypeInfo(11, "日期", TypeCategory.DATE),
            new TypeInfo(18, "数组", TypeCategory.CLASS),
            new TypeInfo(19, "可空", TypeCategory.CLASS),
    };

    @Transactional
    public void execute() {
        List<TypePO> primTypes = typeMapper.getPrimitiveTypes();
        Set<Long> existingIds = new HashSet<>(NncUtils.map(primTypes, TypePO::getId));
        List<TypeInfo> types = new ArrayList<>();
        for (TypeInfo typeInfo : TYPE_INFO_LIST) {
            if(!existingIds.contains(typeInfo.id)) {
                types.add(typeInfo);
            }
        }
        initType(types);
    }

    private void initType(List<TypeInfo> types) {
        List<TypePO> typePOs = new ArrayList<>();
        for (TypeInfo typeInfo : types) {
            TypePO typePO = new TypePO();
            typePO.setTenantId(-1L);
            typePO.setId(typeInfo.id);
            typePO.setCategory(typeInfo.category.code());
            typePO.setName(typeInfo.name);
            typePO.setEphemeral(true);
            typePO.setAnonymous(false);
            typePO.setDesc(typeInfo.name);
            typePOs.add(typePO);
        }
        typeMapper.batchInsert(typePOs);
    }

}
