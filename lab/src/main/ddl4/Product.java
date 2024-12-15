import javax.annotation.Nullable;
import org.metavm.api.EntityField;

public class Product {

    public static final ProductStatus DEFAULT_STATUS = ProductStatus.AVAILABLE;

    //    private String name;
    private Price price;
    private String brand;
    @EntityField(removed = true)
    private @Nullable String description;
    @EntityField(tag = 0)
    private ProductStatus status = ProductStatus.AVAILABLE;;
    private String tag = "none";

    public Product(String name, Price price, String brand) {
//        this.name = name;
        this.price = price;
        this.brand = brand;
    }

//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    private String __brand__() {
        return "unknown";
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}