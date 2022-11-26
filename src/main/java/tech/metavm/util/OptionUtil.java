package tech.metavm.util;

import tech.metavm.object.meta.EnumConstantRT;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.rest.dto.ChoiceOptionDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class OptionUtil {

    public static List<ChoiceOptionDTO> getOptionDTOs(Type enumModel, Set<Long> selected) {
        List<EnumConstantRT> options = enumModel.getEnumConstants();
        return NncUtils.sortAndMap(
                options,
                Comparator.comparingInt(EnumConstantRT::getOrdinal),
                opt -> opt.toChoiceOptionDTO(selected.contains(opt.getId()))
        );
    }

    public static Object getDefaultValue(List<EnumConstantRT> defaultOptions, boolean multiValued) {
        if(multiValued) {
            return NncUtils.map(defaultOptions, EnumConstantRT::getId);
        }
        else {
            return NncUtils.isEmpty(defaultOptions) ? null : defaultOptions.get(0).getId();
        }
    }

}
