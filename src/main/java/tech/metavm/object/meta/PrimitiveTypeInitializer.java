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
            String name,
            TypeCategory category
    ) {
    }

    public static final TypeInfo[] TYPE_INFO_LIST = new TypeInfo[] {
            new TypeInfo("文本", TypeCategory.STRING),
            new TypeInfo("是否", TypeCategory.BOOL),
            new TypeInfo("数值", TypeCategory.NUMBER),
            new TypeInfo("整数", TypeCategory.INT64),
            new TypeInfo("时间", TypeCategory.TIME),
            new TypeInfo("日期", TypeCategory.DATE),
            new TypeInfo("INT32", TypeCategory.INT32)
    };

    @Transactional
    public void execute() {
        List<TypePO> primTypes = typeMapper.getPrimitiveTypes();
        Set<Integer> existingCodes = new HashSet<>(NncUtils.map(primTypes, TypePO::getCategory));
        List<TypeInfo> types = new ArrayList<>();
        for (TypeInfo typeInfo : TYPE_INFO_LIST) {
            if(!existingCodes.contains(typeInfo.category.code())) {
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
