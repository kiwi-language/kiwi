package org.metavm.compiler;

import org.metavm.compiler.util.List;
import org.metavm.util.ApiNamedObject;
import org.metavm.util.BusinessException;

public class KiwiTest2 extends KiwiTestBase {

    public void testEnumToString() {
        deploy("kiwi/to_string/enum_to_string.kiwi");
        var r = callMethod(ApiNamedObject.of("lab"), "formatMoney", List.of(
                100, ApiNamedObject.of("to_string.Currency", "CNY")
        ));
        assertEquals("100.0 CNY", r);
    }

    public void testException() {
        deploy("kiwi/exception/exception.kiwi");
        try {
            callMethod(ApiNamedObject.of("lab"), "raise", List.of("error"));
        } catch (BusinessException e) {
            assertEquals("error", e.getMessage());
        }
    }

}
