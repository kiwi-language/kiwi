package tech.metavm.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ListMatcher<T> extends BaseMatcher<List<T>> {

    public static <T> ListMatcher<T> of(T...values) {
        return of(Arrays.asList(values));
    }

    public static <T> ListMatcher<T> of(List<T> values) {
        return new ListMatcher<>(values);
    }

    private final List<T> values;

    public ListMatcher(List<T> values) {
        this.values = values;
    }

    @Override
    public boolean matches(Object actual) {
        if(actual instanceof List<?> list) {
            if(list.size() != values.size()) {
                return false;
            }
            Iterator<T> it1 = values.iterator();
            Iterator<?> it2 = list.iterator();
            while (it1.hasNext() && it2.hasNext()) {
                if(DiffUtils.isPojoDifferent(it1.next(), it2.next())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(values.toString());
    }
}
