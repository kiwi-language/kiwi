public class Product {
//    private String name;
    private Price price;
    private String brand;

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

}