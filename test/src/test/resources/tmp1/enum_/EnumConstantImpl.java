package enum_;

public class EnumConstantImpl {

    public static String getOptionDesc(Option option) {
        return option.getDesc();
    }

    public enum Option {
        op1 {
            @Override
            public String getDesc() {
                return "Option 1";
            }
        },

        op2 {
            @Override
            public String getDesc() {
                return "Option 2";
            }
        }
        ;

        public abstract String getDesc();

    }


}
