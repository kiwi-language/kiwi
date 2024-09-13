package innerclass.service;

import innerclass.Warehouse;
import org.metavm.api.Component;

import java.util.List;

@Component
public class WarehouseService {

    public Warehouse<String> createWarehouse(String name) {
        return new Warehouse<>(name);
    }

    public Warehouse<String>.Container<String>  createContainer(Warehouse<String> warehouse, String id) {
        return warehouse.new Container<String>(id);
    }

    public Warehouse<String>.Container<Warehouse<String>.Container<String>> createContainer2(Warehouse<String> warehouse, Warehouse<String>.Container<String> id) {
        return warehouse.new Container<Warehouse<String>.Container<String>>(id);
    }

    public Warehouse<String>.Container<Warehouse<String>.Container<String>[][]> createContainer3(Warehouse<String> warehouse, Warehouse<String>.Container<String>[][] id) {
        return warehouse.new Container<Warehouse<String>.Container<String>[][]>(id);
    }

    public List<Warehouse<String>.Container<Warehouse<String>.Container<String>>> createContainer3(Warehouse<String> warehouse, Warehouse<String>.Container<String> id) {
        return List.of(warehouse.new Container<Warehouse<String>.Container<String>>(id));
    }

    public Warehouse<String>.Container<String>.Item<String> createItem(Warehouse<String>.Container<String> container, String type) {
        return container.new Item<String>(type);
    }

    public interface SubService {

       Warehouse<String>.Container<Warehouse<String>.Container<String>>[] createContainer3(Warehouse<String> warehouse, Warehouse<String>.Container<String> id);

    }

}
