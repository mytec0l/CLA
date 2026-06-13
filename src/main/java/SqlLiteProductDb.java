import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlLiteProductDb implements ProductDb {

    private final Connection connection;

    public SqlLiteProductDb(String dbUrl) {
        try {
            this.connection = DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            throw new RuntimeException("Cant connect to SQLite", e);
        }
        init();
    }

    @Override
    public int insert(Product product) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO product(name, category, quantity, price) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getCategory());
            ps.setInt(3, product.getQuantity());
            ps.setDouble(4, product.getPrice());

            int inserted = ps.executeUpdate();
            if (inserted < 1) throw new RuntimeException("Insert failed");

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new RuntimeException("Insert error: no key");
        } catch (SQLException e) {
            throw new RuntimeException("Cant insert product: " + product, e);
        }
    }

    @Override
    public Optional<Product> getById(int id) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM product WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Cant get product by id: " + id, e);
        }
    }

    @Override
    public Optional<Product> getByName(String name) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM product WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Cant get product by name: " + name, e);
        }
    }

    @Override
    public boolean update(Product product) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE product SET name = ?, category = ?, quantity = ?, price = ? WHERE id = ?")) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getCategory());
            ps.setInt(3, product.getQuantity());
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Cant update product: " + product, e);
        }
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM product WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Cant delete product id: " + id, e);
        }
    }

    @Override
    public List<Product> search(ProductFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM product WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (filter.getName() != null) {
            sql.append(" AND name LIKE ?");
            params.add("%" + filter.getName() + "%");
        }
        if (filter.getCategory() != null) {
            sql.append(" AND category = ?");
            params.add(filter.getCategory());
        }
        if (filter.getMinQty() != null) {
            sql.append(" AND quantity >= ?");
            params.add(filter.getMinQty());
        }
        if (filter.getMaxQty() != null) {
            sql.append(" AND quantity <= ?");
            params.add(filter.getMaxQty());
        }
        if (filter.getMinPrice() != null) {
            sql.append(" AND price >= ?");
            params.add(filter.getMinPrice());
        }
        if (filter.getMaxPrice() != null) {
            sql.append(" AND price <= ?");
            params.add(filter.getMaxPrice());
        }

        int offset = (filter.getPage() - 1) * filter.getPageSize();
        sql.append(" LIMIT ? OFFSET ?");
        params.add(filter.getPageSize());
        params.add(offset);

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            List<Product> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Cant serch product", e);
        }
    }

    @Override
    public int deleteAll() {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM product")) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cant delete all products", e);
        }
    }

    private void init() {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS product (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name VARCHAR(100) NOT NULL,
                        category VARCHAR(100),
                        quantity INT NOT NULL DEFAULT 0,
                        price DOUBLE NOT NULL DEFAULT 0
                    )
                    """);
        } catch (SQLException e) {
            throw new RuntimeException("Cant initialize product table", e);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getDouble("price")
        );
    }
}
