import junit.framework.TestCase;
import org.metavm.util.InflectUtil;

import java.util.Map;

public class InflectUtilTest extends TestCase {

    public static final Map<String, String> pairs = Map.ofEntries(
            Map.entry("information", "information"),
            Map.entry("cookie", "cookies"),
            Map.entry("match", "matches"),
            Map.entry("person", "people"),
            Map.entry("item", "items"),
            Map.entry("summary", "summaries"),
            Map.entry("id", "ids"),
            Map.entry("child", "children")
    );

    public void test() {
        for (Map.Entry<String, String> entry : pairs.entrySet()) {
            String singular = entry.getKey();
            String plural = entry.getValue();
            assertEquals("Singular of " + plural + " should be " + singular, singular, InflectUtil.singularize(plural));
            assertEquals("Plural of " + singular + " should be " + plural, plural, InflectUtil.pluralize(singular));
        }
    }

}

