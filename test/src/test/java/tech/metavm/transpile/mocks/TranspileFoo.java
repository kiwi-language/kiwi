package tech.metavm.transpile.mocks;

public class TranspileFoo {

    public void deduct(Product product) {
        if (product.inventory > 0) {
            product.inventory--;
        }
        else throw new RuntimeException("Out of inventory");
    }

}
