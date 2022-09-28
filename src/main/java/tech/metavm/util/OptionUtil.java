package tech.metavm.util;

import tech.metavm.object.meta.ChoiceOption;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.rest.dto.ChoiceOptionDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class OptionUtil {

    public static List<ChoiceOptionDTO> getOptionDTOs(Type enumModel, Set<Long> selected) {
        List<ChoiceOption> options = enumModel.getChoiceOptions();
        return NncUtils.sortAndMap(
                options,
                Comparator.comparingInt(ChoiceOption::getOrder),
                opt -> opt.toDTO(selected.contains(opt.getId()))
        );
    }

    public static Object getDefaultValue(List<ChoiceOption> defaultOptions, boolean multiValued) {
        if(multiValued) {
            return NncUtils.map(defaultOptions, ChoiceOption::getId);
        }
        else {
            return NncUtils.isEmpty(defaultOptions) ? null : defaultOptions.get(0).getId();
        }
    }

}
