package tech.metavm.util;

import tech.metavm.object.meta.EnumConstant;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.rest.dto.ChoiceOptionDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class OptionUtil {

    public static List<ChoiceOptionDTO> getOptionDTOs(Type enumModel, Set<Long> selected) {
        List<EnumConstant> options = enumModel.getEnumConstants();
        return NncUtils.sortAndMap(
                options,
                Comparator.comparingInt(EnumConstant::getOrdinal),
                opt -> opt.toChoiceOptionDTO(selected.contains(opt.getId()))
        );
    }

    public static Object getDefaultValue(List<EnumConstant> defaultOptions, boolean multiValued) {
        if(multiValued) {
            return NncUtils.map(defaultOptions, EnumConstant::getId);
        }
        else {
            return NncUtils.isEmpty(defaultOptions) ? null : defaultOptions.get(0).getId();
        }
    }

}
