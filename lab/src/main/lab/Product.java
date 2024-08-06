import org.metavm.api.EntityField;

import javax.annotation.Nullable;

public class Product {
    private String name;
    private Price price;
    private @Nullable String description;
    @EntityField(tag = 0)
    private ProductState state = ProductState.AVAILABLE;

    public Product(String name, Price price, @Nullable String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }
}