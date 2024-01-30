package tech.metavm.transpile.mocks;

public class TranspileFoo {

    public void deduct(Product product) {
        if (product.inventory > 0) {
            product.inventory--;
        }
        else throw new RuntimeException("库存不足");
    }

}
