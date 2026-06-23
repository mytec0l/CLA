import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

class StoreHttpServerTest {

    private StoreHttpServer server;
    private String token;
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() throws IOException {
        var db = new SqlLiteProductDb("jdbc:sqlite::memory:");
        server = new StoreHttpServer(0, db);
        server.start();
        RestAssured.port = server.getPort();

        token = login("admin", "1234").path("token");
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    private Response login(String user, String pass) {
        return RestAssured.given()
                .contentType("application/json")
                .body(gson.toJson(Map.of("login", user, "password", pass)))
                .post("/login");
    }

    private RequestSpecification auth() {
        return RestAssured.given().header("Authorization", "Bearer " + token);
    }

    private String product(String name) {
        return gson.toJson(Map.of("name", name, "category", "food", "quantity", 10, "price", 20.0));
    }

    private int createProduct(String name) {
        return auth().contentType("application/json")
                .body(product(name))
                .put("/products")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    @Test
    void loginReturnsToken() {
        login("admin", "1234").then()
                .statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    void requestWithoutTokenIsRejected() {
        RestAssured.given().get("/products/1").then().statusCode(401);
    }

    @Test
    void createProductReturnsId() {
        auth().contentType("application/json")
                .body(product("milk"))
                .put("/products")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("milk"));
    }

    @Test
    void duplicateNameReturns409() {
        createProduct("bread");
        auth().contentType("application/json")
                .body(product("bread"))
                .put("/products")
                .then()
                .statusCode(409);
    }

    @Test
    void getProductById() {
        int id = createProduct("eggs");
        auth().get("/products/" + id).then()
                .statusCode(200)
                .body("name", is("eggs"));
    }

    @Test
    void getNotExistingProductReturns404() {
        auth().get("/products/9999").then().statusCode(404);
    }

    @Test
    void updateProduct() {
        int id = createProduct("juice");
        auth().contentType("application/json")
                .body(gson.toJson(Map.of("name", "juice", "category", "food", "quantity", 99, "price", 20.0)))
                .post("/products/" + id)
                .then()
                .statusCode(200)
                .body("quantity", is(99));
    }

    @Test
    void deleteProduct() {
        int id = createProduct("butter");
        auth().delete("/products/" + id).then().statusCode(200);
        auth().get("/products/" + id).then().statusCode(404);
    }
}