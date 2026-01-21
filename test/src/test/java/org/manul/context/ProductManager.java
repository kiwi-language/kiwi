package org.manul.context;

import lombok.SneakyThrows;
import org.manul.context.sql.TransactionPropagation;
import org.manul.context.sql.Transactional;
import org.manul.jdbc.TransactionStatus;
import org.manul.jdbc.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component(module = "test")
public class ProductManager {

    private final DataSource dataSource;
    private final TransactionTemplate transactionTemplate;
    private long nextId = 10000;

    public ProductManager(DataSource dataSource, TransactionTemplate transactionTemplate) {
        this.dataSource = dataSource;
        this.transactionTemplate = transactionTemplate;
    }

    @Transactional
    @SneakyThrows
    public long create(String name, double price) {
        var con = TransactionStatus.getConnection(dataSource);
        try {
            var pst = con.prepareStatement("insert into product (id, name, price, stock, available) values (?,?,?,?,?)");
            var id = nextId++;
            pst.setLong(1, id);
            pst.setString(2, name);
            pst.setDouble(3, price);
            pst.setLong(4, 100);
            pst.setBoolean(5, true);
            pst.executeUpdate();
            try {
                transactionTemplate.execute(() -> clearStock(id), false, TransactionPropagation.NESTED);
            } catch (Exception ignored) {
            }
            return id;
        } finally {
            returnConnection(con);
        }
    }

    @SneakyThrows
    public Product getProduct(long id) {
        var con = TransactionStatus.getConnection(dataSource);
        try {
            var pst = con.prepareStatement("select id, name, price, stock, available from product where id = ?");
            pst.setLong(1, id);
            var rs = pst.executeQuery();
            if (rs.next()) {
                return new Product(rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getLong("stock"),
                        rs.getBoolean("available")
                        );
            } else
                throw new RuntimeException("Product " + id + " does not exist");
        } finally {
            returnConnection(con);
        }
    }

    @SneakyThrows
    private void clearStock(long productId) {
        var con = TransactionStatus.getConnection(dataSource);
        try {
            var pst = con.prepareStatement("update product set available = false where id = ?");
            pst.setLong(1, productId);
            pst.executeUpdate();

            var pst2 = con.prepareStatement("update product set stock = ? where id = ?");
            // Trigger a deliberate error for non-null constraint
            pst2.setObject(1, null);
            pst2.setLong(2, productId);
            pst2.executeUpdate();
        } finally {
            returnConnection(con);
        }
    }

    private void returnConnection(Connection connection) throws SQLException {
        if (!TransactionStatus.isTransactionActive())
            connection.close();
    }

    @Init(order = 100)
    @SneakyThrows
    public void init() {
        try (var conn = dataSource.getConnection()) {
            var stmt = conn.createStatement();
            stmt.executeUpdate("""
                    create table product (
                        id bigint not null,
                        name varchar(256) not null,
                        price double not null,
                        stock bigint not null,
                        available bool not null
                    )
                    """
            );
        }
    }

}
