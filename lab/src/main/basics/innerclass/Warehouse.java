package innerclass;

public class Warehouse<T> {

    private T location;

    public Warehouse(T location) {
        this.location = location;
    }

    public T getLocation() {
        return location;
    }

    public void setLocation(T location) {
        this.location = location;
    }

    public class Container<C> {

        private final C id;

        public Container(C id) {
            this.id = id;
        }

        public C getId() {
            return id;
        }

        public Warehouse<T> getWarehouse() {
            return Warehouse.this;
        }

        public class Item<I> {
            private final I type;

            public Item(I type) {
                this.type = type;
            }

            public I getType() {
                return type;
            }

            public Container<C> getContainer() {
                return Container.this;
            }

            public Warehouse<T> getWarehouse() {
                return Warehouse.this;
            }

        }

    }

}
