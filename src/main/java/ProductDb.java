import java.util.List;
import java.util.Optional;

public interface ProductDb {

    int insert(Product product);

    Optional<Product> getById(int id);

    Optional<Product> getByName(String name);

    boolean update(Product product);

    boolean delete(int id);

    List<Product> search(ProductFilter filter);

    int deleteAll();
}
