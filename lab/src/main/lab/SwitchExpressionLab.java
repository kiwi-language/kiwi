public class SwitchExpressionLab {

    public static String getNumber(int v) {
        return v < 0 ? "negative" : switch (v) {
            case 0:
                yield "zero";
            case 1:
                yield "one";
            case 2:
                yield "two";
            case 3:
                yield "three";
            default: {
                if (v < 100) {
                    yield "tens";
                } else {
                    yield "more than a hundred";
                }
            }
        };
    }

}
