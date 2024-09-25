package break_;

public class BreakFoo {

    public static boolean contains(Object[][] es, Object o) {
        final int size = es.length;
        int i = 0;
        found:
        {
//            {
                if (o == null) {
                    for (; i < es.length; i++)
                        for (int j = 0; j < es[i].length; j++)
                            if (es[i][j] == null)
                                break found;
                } else {
                    for (; i < es.length; i++)
                        for (int j = 0; j < es[i].length; j++)
                            if (o.equals(es[i][j]))
                                break found;
                }
                return false;
//            }
        }
        return true;
    }

}
