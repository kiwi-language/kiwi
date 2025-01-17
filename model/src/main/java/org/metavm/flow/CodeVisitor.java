package org.metavm.flow;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodeVisitor {

    private final CodeInput input;

    public CodeVisitor(CodeInput input) {
        this.input = input;
    }

    private int read() {
        return input.read();
    }

    private int readShort() {
        return input.readShort();
    }

    /*
    Flow __init_OUT_OF_STOCK__ (): ProductStatus|null {
    new: new shopping.ProductStatus
    ldc: ldc OUT_OF_STOCK
    ldc_1: ldc 2
    ldc_2: ldc 2
    ldc_3: ldc Out of stock
    invokespecial: invokespecial shopping.ProductStatus.ProductStatus(string|null,int,int,string|null)
    ret: return
     */
    public void scan() {
        int b = read();
        switch (b) {
            case Bytecodes.NEW -> {
                readShort();
            }
        }
    }

}
