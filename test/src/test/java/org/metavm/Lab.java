package org.metavm;

import java.util.List;
import java.util.Set;

public class Lab {

    public static final List<String> types = List.of(
            "Double", "Float", "Long", "Int", "Short", "Byte", "Char"
    );

    public static final Set<String> smallIntTypes = Set.of(
            "Short", "Byte", "Char"
    );

    public static void main(String[] args) {
        switch (args.length) {
            default:
                System.out.println(2);
            case 1:
                System.out.println(1);
        }
    }

    public static void test(Object o) {
        switch (o) {
            case String s -> System.out.println(0);
            case Integer i -> System.out.println(1);
            default -> System.out.println(2);
        }
    }

    private static void generateTypeCastCode() {
        var buf = new StringBuilder();
        for (String t1 : types) {
            if (buf.isEmpty()) {
                buf.append("var kind1 = t1.getKind();\n");
                buf.append("var kind2 = t2.getKind();\n");

            } else
                buf.append(" else ");
            buf.append("if (kind1 == JvmPrimitiveTypeKind.")
                    .append(t1.toUpperCase())
                    .append(") {\n");
            boolean first = true;
            for (String t2 : types) {
                if(t1.equals(t2))
                    continue;
                buf.append('\t');
                if (first) {
                    first = false;
                } else
                    buf.append("else ");
                buf.append("if (kind2 == JvmPrimitiveTypeKind.").append(t2.toUpperCase())
                        .append(")\n");
                if (smallIntTypes.contains(t1)) {
                    if (t2.equals("Int"))
                        buf.append("\t\treturn operandNode;\n");
                    else
                        buf.append("\t\t").append("return methodGenerator.createIntTo")
                                .append(t2).append("();\n");
                }
                else {
                    buf.append("\t\t").append("return methodGenerator.create")
                            .append(t1).append("To").append(t2).append("();\n");
                }
            }
            buf.append("}");
        }
        buf.append("\nthrow new InternalException(\"Unrecognized primitive type cast:\" + typeCastExpression.getText());");
        System.out.println(buf);
    }

    private static void generateAsmTypeCastCode() {
        var buf = new StringBuilder();
        for (String t1 : types) {
            if (buf.isEmpty()) {
                buf.append("var kind1 = t1.getKind();\n");
                buf.append("var kind2 = t2.getKind();\n");
            } else
                buf.append(" else ");
            buf.append("if (kind1 == PrimitiveKind.")
                    .append(t1.toUpperCase())
                    .append(") {\n");
            boolean first = true;
            for (String t2 : types) {
                if(t1.equals(t2) || smallIntTypes.contains(t1) && t2.equals("Int"))
                    continue;
                buf.append('\t');
                if (first) {
                    first = false;
                } else
                    buf.append("else ");
                buf.append("if (kind2 == PrimitiveKind.").append(t2.toUpperCase())
                        .append(")\n");
                var s = smallIntTypes.contains(t1) ? "int" : t1.toLowerCase();
                buf.append("\t\t").append("Nodes.")
                        .append(s).append("To").append(t2).append("(code);\n");
            }
            buf.append("}");
        }
        System.out.println(buf);
    }

}
