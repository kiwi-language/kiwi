package tech.metavm;

import tech.metavm.object.instance.core.PhysicalId;

public class Lab {

    public static void main(String[] args) {
        System.out.println(PhysicalId.of(1757015144L).toString());
    }

    private void test(int v) {
        var value = switch(v) {
            case 1 -> {
                yield "one";
            }
            case 2 -> {
                yield "two";
            }
            default -> {
                switch (v) {
                    case 3 -> {
                        yield "three";
                    }
                    case 4 -> {
                        yield "four";
                    }
                    default -> {
                        if (v < 100) {
                            yield "tens";
                        } else {
                            yield "more than a hundred";
                        }
                    }
                }
            }
        };
    }

}
