import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ProductDbTest {

    private ProductDb db;

    @BeforeEach
    void setUp() {
        db = new SqlLiteProductDb("jdbc:sqlite::memory:");
        db.insert(new Product("Рис", "Крупи", 100, 25.0));
        db.insert(new Product("Куриця", "Мясо", 50, 120.0));
        db.insert(new Product("Макарони", "Крупи", 200, 18.0));
        db.insert(new Product("Кока кола", "Напої", 80, 35.0));
    }

    @AfterEach
    void cleanUp() {
        db.deleteAll();
    }

    @Test
    void insertAndGetById() {
        int id = db.insert(new Product("Молоко", "Молочне", 60, 42.0));

        Optional<Product> found = db.getById(id);

        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(new Product(id, "Молоко", "Молочне", 60, 42.0));
    }

    @Test
    void getByName() {
        Optional<Product> found = db.getByName("Рис");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Рис");
        assertThat(found.get().getCategory()).isEqualTo("Крупи");
    }

    @Test
    void updateProduct() {
        int id = db.insert(new Product("Тест", "Крупи", 10, 5.0));
        Product product = db.getById(id).get();
        product.setPrice(99.0);
        product.setQuantity(999);

        boolean updated = db.update(product);

        assertThat(updated).isTrue();
        Product refreshed = db.getById(id).get();
        assertThat(refreshed.getPrice()).isEqualTo(99.0);
        assertThat(refreshed.getQuantity()).isEqualTo(999);
    }

    @Test
    void deleteProduct() {
        int id = db.insert(new Product("Тест", "Крупи", 1, 1.0));
        assertThat(db.getById(id)).isPresent();

        boolean deleted = db.delete(id);

        assertThat(deleted).isTrue();
        assertThat(db.getById(id)).isEmpty();
    }

    @Test
    void searchByName() {
        List<Product> results = db.search(new ProductFilter().setName("рон"));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Макарони");
    }

    @Test
    void searchByCategory() {
        List<Product> results = db.search(new ProductFilter().setCategory("Крупи"));

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Product::getName).containsExactlyInAnyOrder("Рис", "Макарони");
    }

    @Test
    void searchByPriceRange() {
        List<Product> results = db.search(new ProductFilter().setMinPrice(20.0).setMaxPrice(40.0));

        assertThat(results).extracting(Product::getName).containsExactlyInAnyOrder("Рис", "Кока кола");
    }

    @Test
    void searchByQtyRange() {
        List<Product> results = db.search(new ProductFilter().setMinQty(80).setMaxQty(150));

        assertThat(results).extracting(Product::getName).containsExactlyInAnyOrder("Рис", "Кока кола");
    }

    @Test
    void searchCombined() {
        List<Product> results = db.search(new ProductFilter().setCategory("Крупи").setMinPrice(20.0));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Рис");
    }

    @Test
    void searchPagination() {
        db.deleteAll();
        for (int i = 1; i <= 5; i++) {
            db.insert(new Product("Товар" + i, "Тест", i * 10, i * 5.0));
        }

        List<Product> page2 = db.search(new ProductFilter().setPage(2).setPageSize(2));

        assertThat(page2).hasSize(2);
        assertThat(page2).extracting(Product::getName).containsExactly("Товар3", "Товар4");
    }
}
