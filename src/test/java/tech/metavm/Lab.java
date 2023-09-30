package tech.metavm;

import tech.metavm.autograph.mocks.AstCouponState;
import tech.metavm.autograph.mocks.AstProduct;
import tech.metavm.autograph.mocks.DirectAstCoupon;

import java.util.List;

public class Lab {

    public static void main(String[] args) {
    }

    public String test(Object value, int num) {
        String result;
        var breakVar = false;
        out:
        {
            if (num == 0) {
                switch (value) {
                    case String str -> {
                        if (str.equals("empty")) {
                            result = "";
                            breakVar = true;
                        } else {
                            result = str;
                        }
                    }
                    case Long l -> {
                        result = l + "L";
                    }
                    default -> {
                        result = value.toString();
                    }
                }
            } else {
                result = "else";
            }
        }
        return result;
    }

    public String testSwitch(Object value) {
        String result;
        boolean _break = false;
        switch (value) {
            case String str -> {
                if (!str.isEmpty()) {
                    if (str.equals("noop")) {
                        result = "empty";
                        _break = true;
                    } else {
                        if (str.equals("what")) {
                            result = "?";
                            break;
                        }
                        result = str;
                    }
                    if (str.equals("what")) {
                        result = "?";
                        _break = true;
                    } else {
                        result = str;
                    }
                } else {
                    result = str;
                }
                result = str;
            }
            case Long l -> {
                result = l + "L";
            }
            default -> result = value.toString();
        }
        return result;
    }


    private void __extraLoopTest__(boolean condition) {
    }

}