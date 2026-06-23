import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.gson.Gson;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StoreHttpServer {

    private static final String SECRET = "secret";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "1234";
    private static final Gson GSON = new Gson();

    private final HttpServer server;
    private final ProductDb db;

    public StoreHttpServer(int port, ProductDb db) throws IOException {
        this.db = db;
        server = HttpServer.create(new InetSocketAddress(port), 0);
        createEndpoints();
    }

    public void start() {
        server.start();
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    public void stop() {
        server.stop(0);
    }

    private void createEndpoints() {
        server.createContext("/login", this::handleLogin);

        HttpContext productsContext = server.createContext("/products", this::handleProducts);
        productsContext.setAuthenticator(new BearerAuthenticator());

        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(404, 0);
            exchange.getResponseBody().close();
        });
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(405, 0);
            exchange.getResponseBody().close();
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes());
        Map<?, ?> creds = GSON.fromJson(body, Map.class);
        String login = (String) creds.get("login");
        String password = (String) creds.get("password");

        if (!USERNAME.equals(login) || !PASSWORD.equals(password)) {
            sendJson(exchange, 401, "{}");
            return;
        }

        String token = JWT.create()
                .withSubject(login)
                .sign(Algorithm.HMAC256(SECRET));

        sendJson(exchange, 200, GSON.toJson(Map.of("token", token)));
    }

    private void handleProducts(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String idPart = path.replaceFirst("^/products/?", "");

        if (method.equals("PUT") && idPart.isEmpty()) {
            handleCreate(exchange);
        } else if (!idPart.isEmpty()) {
            try {
                int id = Integer.parseInt(idPart);
                switch (method) {
                    case "GET" -> handleGetById(exchange, id);
                    case "POST" -> handleUpdate(exchange, id);
                    case "DELETE" -> handleDelete(exchange, id);
                    default -> {
                        exchange.sendResponseHeaders(405, 0);
                        exchange.getResponseBody().close();
                    }
                }
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(400, 0);
                exchange.getResponseBody().close();
            }
        } else {
            exchange.sendResponseHeaders(405, 0);
            exchange.getResponseBody().close();
        }
    }

    private void handleGetById(HttpExchange exchange, int id) throws IOException {
        Optional<Product> product = db.getById(id);
        if (product.isEmpty()) {
            sendJson(exchange, 404, "{}");
            return;
        }
        sendJson(exchange, 200, GSON.toJson(product.get()));
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        Product product = GSON.fromJson(body, Product.class);

        if (db.getByName(product.getName()).isPresent()) {
            sendJson(exchange, 409, "{}");
            return;
        }

        int id = db.insert(product);
        product.setId(id);
        sendJson(exchange, 201, GSON.toJson(product));
    }

    private void handleUpdate(HttpExchange exchange, int id) throws IOException {
        if (db.getById(id).isEmpty()) {
            sendJson(exchange, 404, "{}");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes());
        Product product = GSON.fromJson(body, Product.class);
        product.setId(id);
        db.update(product);
        sendJson(exchange, 200, GSON.toJson(product));
    }

    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        boolean deleted = db.delete(id);
        if (!deleted) {
            sendJson(exchange, 404, "{}");
            return;
        }
        sendJson(exchange, 200, "{}");
    }

    private void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static class BearerAuthenticator extends Authenticator {
        @Override
        public Result authenticate(HttpExchange exchange) {
            List<String> headers = exchange.getRequestHeaders().get("Authorization");
            if (headers == null || headers.isEmpty()) return new Failure(401);

            String[] parts = headers.getFirst().split(" ");
            if (parts.length != 2 || !parts[0].equals("Bearer")) return new Failure(401);

            try {
                JWT.require(Algorithm.HMAC256(SECRET)).build().verify(parts[1]);
                return new Success(new HttpPrincipal(JWT.decode(parts[1]).getSubject(), "store"));
            } catch (JWTVerificationException e) {
                return new Failure(401);
            }
        }
    }
}