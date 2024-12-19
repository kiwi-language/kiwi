package enum_;

public enum EnumFoo {

    option1(1) {
        @Override
        public String getDesc() {
            return "Option 1";
        }
    },

    option2(2) {
        @Override
        public String getDesc() {
            return "Option 2";
        }
    }
    ;

    public static final EnumFoo DEFAULT = option1;

    private final int code;

    EnumFoo(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public abstract String getDesc();

}
